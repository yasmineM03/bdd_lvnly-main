import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class SalleDeVenteQuery {

    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lecontev";
    static final String PASSWD = "lecontev";

    private Connection conn;

    public SalleDeVenteQuery() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            conn.setAutoCommit(false); 
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données :");
            e.printStackTrace();
        }
    }

    public void createSalleDeVente(Scanner scanner) {
    try {
        // On récupère les infos de la salle
        String sqlGetCat = "SELECT DISTINCT categorie FROM Produits";
        PreparedStatement stmtCat = conn.prepareStatement(sqlGetCat);
        ResultSet resultCat = stmtCat.executeQuery();
        System.out.println("Les catégories disponibles :");
        while (resultCat.next()){
            String categorie = resultCat.getString("categorie");
            System.out.println(categorie);
        }
        
        System.out.print("Catégorie de la Salle : ");
        String categorie = scanner.nextLine();

        int typeEnchere;
        while (true) {
            System.out.print("Type d'enchère (0 pour Montante, 1 pour Descendante) : ");
            if (scanner.hasNextInt()) {
                typeEnchere = scanner.nextInt();
                if (typeEnchere == 0 || typeEnchere == 1) {
                    scanner.nextLine();
                    break;
                }
            }
            System.out.println("Veuillez entrer 0 pour une enchère montante ou 1 pour une enchère descendante.");
            scanner.nextLine();
        }

        
        int choixDuree;
        while (true) {
            System.out.print("La durée est-elle limitée ? (0 pour Illimitée, 1 pour Limitée) : ");
            if (scanner.hasNextInt()) {
                choixDuree = scanner.nextInt();
                scanner.nextLine(); 
                if (choixDuree == 0 || choixDuree == 1) {
                    break; 
                } else {
                    System.out.println("Entrée invalide. Veuillez entrer 0 pour illimitée ou 1 pour limitée.");
                }
            } else {
                System.out.println("Entrée invalide. Veuillez entrer un nombre.");
                scanner.nextLine();
            }
        }

        int revocable;
        while (true) {
            System.out.print("La salle est-elle révocable ? (0 pour Non, 1 pour Oui) : ");
            if (scanner.hasNextInt()) {
                revocable = scanner.nextInt();
                scanner.nextLine();
                if (revocable == 0 || revocable == 1) {
                    break;
                }
            }
            System.out.println("Veuillez entrer 0 pour Non ou 1 pour Oui.");
            scanner.nextLine();
        }
        //On crée la salle dans sql avec les informations donnnées par l'utilisateur
        String sqlCreateSalle = "BEGIN INSERT INTO SalleDeVente (categorie, croissance, duree, revocable) " + "VALUES (?, ?, ?, ?) RETURNING IDSalle INTO ?; END;";
        CallableStatement stmtSalle = conn.prepareCall(sqlCreateSalle);
        stmtSalle.setString(1, categorie);
        stmtSalle.setInt(2, typeEnchere);
        stmtSalle.setObject(3, choixDuree, Types.INTEGER); 
        stmtSalle.setInt(4, revocable);
        stmtSalle.registerOutParameter(5, Types.NUMERIC); 
        stmtSalle.execute();
        int idSalle = stmtSalle.getInt(5); 
        System.out.println("\nSalle créée avec ID : " + idSalle);


        //On choisit les produits à mettre en vente
        String sqlGetProduits = "SELECT idProduit, nom, prixRevient, stock FROM Produits WHERE categorie = ? AND stock > 0 AND disponible = 1";
        PreparedStatement stmtProduits = conn.prepareStatement(sqlGetProduits);
        stmtProduits.setString(1, categorie);
        ResultSet rs = stmtProduits.executeQuery();

        System.out.println("\nProduits disponibles dans la catégorie '" + categorie + "':");
        while (rs.next()) {
            System.out.printf("ID: %d | Nom: %s | Prix Revient: %.2f | Stock: %d\n",
                              rs.getInt("idProduit"),
                              rs.getString("nom"),
                              rs.getDouble("prixRevient"),
                              rs.getInt("stock"));
        }
        long dateStart = 0;
        if(typeEnchere == 1){
            dateStart = System.currentTimeMillis();
        }
        //Ajouter des produits à la salle
        while (true) {
            System.out.print("\nEntrez l'ID du produit à ajouter (ou -1 pour terminer) : ");
            int idProduit = scanner.nextInt();
            if (idProduit == -1) break;

            System.out.print("Entrez le prix de départ pour ce produit : ");
            double prixDepart = scanner.nextDouble();
            scanner.nextLine(); 
            if(choixDuree == 1){
                System.out.print("Entrez la durée souhaitée pour la vente de ce produit (en secondes): ");
                int dureeUtilisateur = scanner.nextInt();
                long startTime = System.currentTimeMillis();
                scanner.nextLine();
                String sqlAddProduit = "INSERT INTO Vente (idSalle, idProduit, prixDepart,dateLimite,dateDebut) VALUES (?, ?, ?,?,?)";
                PreparedStatement stmtAddProduit = conn.prepareStatement(sqlAddProduit);
                stmtAddProduit.setInt(1, idSalle);
                stmtAddProduit.setInt(2, idProduit);
                stmtAddProduit.setDouble(3, prixDepart);
                stmtAddProduit.setLong(4,startTime + dureeUtilisateur *1000);
                stmtAddProduit.setLong(5,dateStart);
                stmtAddProduit.executeUpdate();
                String sqlUpdateProduit = "UPDATE Produits SET disponible = 0 WHERE idProduit = ?";
                PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                stmtUpdateProduit.setInt(1, idProduit);
                stmtUpdateProduit.executeUpdate();
            }
            else{
                String sqlAddProduit = "INSERT INTO Vente (idSalle, idProduit, prixDepart,dateDebut) VALUES (?, ?, ?,?)";
                PreparedStatement stmtAddProduit = conn.prepareStatement(sqlAddProduit);
                stmtAddProduit.setInt(1, idSalle);
                stmtAddProduit.setInt(2, idProduit);
                stmtAddProduit.setDouble(3, prixDepart);
                stmtAddProduit.setLong(4,dateStart);
                stmtAddProduit.executeUpdate();
                


            }
            String sqlCheckProduct = "SELECT disponible FROM produits WHERE idProduit = ?";
            PreparedStatement stmtCheckProduct = conn.prepareStatement(sqlCheckProduct);
            stmtCheckProduct.setInt(1, idProduit);

            ResultSet rss = stmtCheckProduct.executeQuery();

            if (rss.next()) { 
                int disponible = rss.getInt("disponible");
                
                if (disponible == 0) {
                    // Si les produits sont pas disponibles, on rollback
                    conn.rollback();
                    System.out.println("Produit non disponible, transaction annulée.");
                    return; 
                } else {
                    // Sinon, effectuer le commit
                    String sqlUpdateProduit = "UPDATE Produits SET disponible = 0 WHERE idProduit = ?";
                    PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                    stmtUpdateProduit.setInt(1, idProduit);
                    stmtUpdateProduit.executeUpdate();
                    conn.commit();
                    System.out.println("Produit ID " + idProduit + " ajouté avec un prix de départ de " + prixDepart);
                }
            } else {
                // Si aucun produit trouvé, rollback pour sécurité
                conn.rollback();
                System.out.println("Produit non trouvé, transaction annulée.");
                return;
            }
            
        }
        // Si la transaction se passe bien , on peut commit dans la table
        conn.commit();
        System.out.println("\nSalle de vente créée avec succès et produits ajoutés.");

    } catch (SQLException e) {
        // En cas d'erreur on rollback pour annuler la transaction
        System.err.println("Erreur lors de la création de la salle de vente. Annulation de la transaction.");
        try {
            conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
    }
}

    private void displayVente(Scanner scanner, int idSalle) {
    try {
        String sqlGetVentes = "SELECT Vente.idVente, Produits.idProduit, Nom, PrixDepart, DateLimite " +
                              "FROM Vente, Produits " +
                              "WHERE Vente.idProduit = Produits.idProduit AND Vente.idSalle = ?";
        PreparedStatement stmtVente = conn.prepareStatement(sqlGetVentes);
        stmtVente.setInt(1, idSalle);
        ResultSet rs = stmtVente.executeQuery();

        System.out.println("Produits disponibles à la vente dans la salle " + idSalle + " :");
        while (rs.next()) {
            System.out.printf("ID Vente: %d | ID Produit: %d | Nom: %s | Prix Départ: %.2f | Date Limite: %s\n",
                              rs.getInt("idVente"),
                              rs.getInt("idProduit"),
                              rs.getString("Nom"),
                              rs.getDouble("PrixDepart"),
                              rs.getTimestamp("DateLimite"));
        }

        while (true) {
            System.out.print("\nEntrez l'ID de la vente pour enchérir (ou -1 pour revenir au menu) : ");
            int idVente = scanner.nextInt();
            scanner.nextLine();

            if (idVente == -1) break;

            System.out.print("Entrez le montant de votre enchère : ");
            double montant = scanner.nextDouble();
            scanner.nextLine();

            String sqlRemoveVente = "DELETE FROM Vente WHERE idVente = ?";
            PreparedStatement stmtRemove = conn.prepareStatement(sqlRemoveVente);
            stmtRemove.setInt(1, idVente);

            String sqlUpdateProduit = "UPDATE Produits SET disponible = 0 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
            PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
            stmtUpdateProduit.setInt(1, idVente);

            try {
                conn.setAutoCommit(false);
                stmtRemove.executeUpdate();
                stmtUpdateProduit.executeUpdate();
                conn.commit();
                System.out.println("Votre enchère a été acceptée. La vente est maintenant terminée.");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erreur lors de l'enchère. Transaction annulée.");
                e.printStackTrace();
            }
        }

    } catch (SQLException e) {
        System.err.println("Erreur lors de l'affichage des ventes.");
        e.printStackTrace();
    }
}
}