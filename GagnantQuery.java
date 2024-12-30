import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

public class GagnantQuery {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lecontev";
    static final String PASSWD = "lecontev";

    private Connection conn;
    public GagnantQuery() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            conn.setAutoCommit(false); 
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données :");
            e.printStackTrace();
        }
    }

    public void chercheGagnant(Scanner scanner) throws Exception {
        System.out.println("Recherche du gagnant d'une vente :");
        String sqlGetVente = """
        SELECT v.idVente, p.nom AS nomProduit
        FROM Vente v
        JOIN Produits p ON v.idProduit = p.idProduit
        """;
            PreparedStatement stmtVente = conn.prepareStatement(sqlGetVente);
            ResultSet resultVente = stmtVente.executeQuery();
            System.out.println("Les ventes disponibles :");
            while (resultVente.next()){
                int idVente = resultVente.getInt("idVente");
                String nomProduit = resultVente.getString("nomProduit");
                System.out.printf("ID VENTE : %d | Produit %s\n  ", idVente,nomProduit);
            }
        System.out.print("Entrez l'ID de la vente : ");
        int idVente = scanner.nextInt();
        scanner.nextLine();

        String sqlCheckVente = """
        SELECT idProduit FROM Vente WHERE idVente = ?
        """;
        PreparedStatement stmtCheckVente = conn.prepareStatement(sqlCheckVente);
        stmtCheckVente.setInt(1, idVente);
        ResultSet rsCheck = stmtCheckVente.executeQuery();

        if (!rsCheck.next()) {
            System.out.println("Erreur : La vente avec l'ID " + idVente + " n'existe pas.");
            return;
        }

        // récupérer la salle de vente
        String sqlSalle = """
        SELECT revocable, duree, croissance FROM SalleDeVente 
        WHERE idSalle = (SELECT idSalle FROM Vente WHERE idVente = ?)
        """;
        PreparedStatement stmtSalle = conn.prepareStatement(sqlSalle);
        stmtSalle.setInt(1, idVente);
        ResultSet rsSalle = stmtSalle.executeQuery();

        if (rsSalle.next()){
            int duree = rsSalle.getInt("duree");
            int revocable = rsSalle.getInt("revocable");
            int croissance = rsSalle.getInt("croissance");

            if (croissance == 1){
                // enchere descendante
                String sqlOffer = "SELECT prix, email FROM Offre WHERE idVente = ? ";
                PreparedStatement stmtOffer = conn.prepareStatement(sqlOffer);
                stmtOffer.setInt(1,idVente);
                ResultSet rsOffer = stmtOffer.executeQuery();
                if (rsOffer.next()){
                    String email = rsOffer.getString("email");
                    int prixOffre = rsOffer.getInt("prix");
                    System.out.println("L'utilisateur" + email + "a gagné cette vente en payant : " + prixOffre);
                } else {
                    System.out.println("Pas encore d'offre pour cette vente");
                    String sqlPrixActuel = "SELECT prixDepart FROM Vente WHERE idVente = ?";
                    PreparedStatement stmtPrixActuel = conn.prepareStatement(sqlPrixActuel);
                    stmtPrixActuel.setInt(1, idVente);
                    ResultSet rsPrixActuel = stmtPrixActuel.executeQuery();

                    if (rsPrixActuel.next()) {
                        int prixActuel = rsPrixActuel.getInt("prixDepart");

                        if (prixActuel == 0) {
                            // Si le prix est descendu à 0, clôturer la vente
                            System.out.println("Le prix de la vente descendante est arrivé à 0. Clôture de la vente.");

                            // Supprimer les offres (par sécurité, même s'il n'y en a pas)
                            String sqlDeleteOffres = "DELETE FROM Offre WHERE idVente = ?";
                            PreparedStatement stmtDeleteOffres = conn.prepareStatement(sqlDeleteOffres);
                            stmtDeleteOffres.setInt(1, idVente);
                            stmtDeleteOffres.executeUpdate();

                            // Remettre le produit disponible
                            String sqlUpdateProduit = "UPDATE Produits SET disponible = 1 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
                            PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                            stmtUpdateProduit.setInt(1, idVente);
                            stmtUpdateProduit.executeUpdate();

                            // Supprimer la vente
                            String sqlDeleteVente = "DELETE FROM Vente WHERE idVente = ?";
                            PreparedStatement stmtDeleteVente = conn.prepareStatement(sqlDeleteVente);
                            stmtDeleteVente.setInt(1, idVente);
                            stmtDeleteVente.executeUpdate();

                            // Commit des modifications
                            conn.commit();

                            System.out.println("La vente descendante a été fermée et le produit est remis disponible.");
                        } else {
                            long dateLimite = 0;
                            if (duree == 1){
                                String sqlDateVente = "SELECT datelimite FROM vente WHERE idVente = ?";
                                PreparedStatement stmtDateVente = conn.prepareStatement(sqlDateVente);
                                stmtDateVente.setInt(1, idVente);
                                ResultSet resultDate = stmtDateVente.executeQuery();
                                resultDate.next();
                                dateLimite = resultDate.getLong("datelimite");
                                if (System.currentTimeMillis() >= dateLimite){
                                    // Annuler la vente et remettre le produit disponible
                                System.out.println("Pas d'offre dans le temps imparti, vente annulée.");

                                // Supprimer les offres liées à cette vente
                                String sqlDeleteOffre = "DELETE FROM Offre WHERE idVente = ?";
                                PreparedStatement stmtDeleteOffre = conn.prepareStatement(sqlDeleteOffre);
                                stmtDeleteOffre.setInt(1, idVente);
                                stmtDeleteOffre.executeUpdate();

                                // Remettre le produit disponible
                                String sqlUpdateProduit = "UPDATE Produits SET disponible = 1 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
                                PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                                stmtUpdateProduit.setInt(1, idVente);
                                stmtUpdateProduit.executeUpdate();

                                // Supprimer la vente
                                String sqlDeleteVenteR = "DELETE FROM Vente WHERE idVente = ?";
                                PreparedStatement stmtDeleteVenteR = conn.prepareStatement(sqlDeleteVenteR);
                                stmtDeleteVenteR.setInt(1, idVente);
                                stmtDeleteVenteR.executeUpdate();

                                // Commit des modifications
                                conn.commit();
                                    return;
                                }
                        }
                    }
                }                
            }}   else{

                int dateLimite = 0;
                if (duree == 1){
                    String sqlDateVente = "SELECT datelimite FROM vente WHERE idVente = ?";
                    PreparedStatement stmtDateVente = conn.prepareStatement(sqlDateVente);
                    stmtDateVente.setInt(1, idVente);
                    ResultSet resultDate = stmtDateVente.executeQuery();
                    resultDate.next();
                    dateLimite = resultDate.getInt("datelimite");
                    if (System.currentTimeMillis() < dateLimite){
                        System.out.println("L'enchère n'est pas finie, pas encore de gagnant.");
                        
                        return;
                    }
                }
                // si 10 min écoulé 
                String sqlLastOfferTime = """
                SELECT o1.time AS lastOfferTime, o1.email, o1.prix
                FROM Offre o1
                WHERE o1.idVente = ?
                ORDER BY o1.prix DESC
                FETCH FIRST 1 ROW ONLY
                """;
                PreparedStatement stmtLastOffer = conn.prepareStatement(sqlLastOfferTime);
                stmtLastOffer.setInt(1, idVente);
                ResultSet rsLastOffer = stmtLastOffer.executeQuery();
                if (rsLastOffer.next()){
                    long lastOfferTime = rsLastOffer.getLong("lastOfferTime");
                    String emailGagnant = rsLastOffer.getString("email");
                    int prixGagnant = rsLastOffer.getInt("prix");
                    if (System.currentTimeMillis() - lastOfferTime > 6000 || (duree == 1 && System.currentTimeMillis() >= dateLimite)) {
                    // Clôturer l'enchère
                    System.out.println("10 minutes écoulée ou vente terminée. L'enchère est terminée.");
                    // Afficher le gagnant si non revocable
                    if (revocable == 0){
                        System.out.println("Le gagnant est " + emailGagnant);
                        System.out.println("Il a payé : " + prixGagnant);
                    } else {
                        String sqlPrixRevient = """
                        SELECT prixrevient FROM Produits 
                        WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)
                        """;
                        PreparedStatement stmtPrixRevient = conn.prepareStatement(sqlPrixRevient);
                        stmtPrixRevient.setInt(1, idVente);
                        ResultSet rsPrixRevient = stmtPrixRevient.executeQuery();
        
                        if (rsPrixRevient.next()) {
                            double prixRevient = rsPrixRevient.getDouble("prixrevient"); // Récupérer le prix de revient du produit

                            System.out.println("Valeur de revocable : " + revocable);
                            // Calculer le coût total de la vente
                            if (prixGagnant < prixRevient) {
                            // Si la vente est révocable et que l'offre gagnante est inférieure au coût de revient, annuler la vente

                            // Annuler la vente et remettre le produit disponible
                            System.out.println("La vente est révocable, mais l'offre gagnante n'est pas rentable. Annulation de la vente.");

                            // Supprimer les offres liées à cette vente
                            String sqlDeleteOffre = "DELETE FROM Offre WHERE idVente = ?";
                            PreparedStatement stmtDeleteOffre = conn.prepareStatement(sqlDeleteOffre);
                            stmtDeleteOffre.setInt(1, idVente);
                            stmtDeleteOffre.executeUpdate();

                            // Remettre le produit disponible
                            String sqlUpdateProduit = "UPDATE Produits SET disponible = 1 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
                            PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                            stmtUpdateProduit.setInt(1, idVente);
                            stmtUpdateProduit.executeUpdate();

                            // Supprimer la vente
                            String sqlDeleteVenteR = "DELETE FROM Vente WHERE idVente = ?";
                            PreparedStatement stmtDeleteVenteR = conn.prepareStatement(sqlDeleteVenteR);
                            stmtDeleteVenteR.setInt(1, idVente);
                            stmtDeleteVenteR.executeUpdate();

                            // Commit des modifications
                            conn.commit();
                            } else {
                                // Vente rentable, conserver la vente et marquer l'utilisateur comme gagnant

                                // Mettre à jour le produit comme vendu (disponible = 0)
                                String sqlUpdateProduit = "UPDATE Produits SET disponible = 0 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
                                PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                                stmtUpdateProduit.setInt(1, idVente);
                                stmtUpdateProduit.executeUpdate();

                                // Afficher le gagnant
                                System.out.println("Félicitations ! L'utilisateur " + emailGagnant + " a gagné l'enchère.");
                                System.out.println("Il a payé : " + prixGagnant + " pour cette vente.");

                                // Commit des modifications
                                conn.commit();
                            }
                }
                
                
                
                
        }
                    }
                    
            } else{
                System.out.println("Pas d'offre pour cette vente.");

            if (duree == 1) { // Si la vente a une durée limitée
                // Récupérer la date limite
                String sqlDateVente = "SELECT datelimite FROM Vente WHERE idVente = ?";
                PreparedStatement stmtDateVente = conn.prepareStatement(sqlDateVente);
                stmtDateVente.setInt(1, idVente);
                ResultSet resultDate = stmtDateVente.executeQuery();

                if (resultDate.next()) {
                    Long datelimite = resultDate.getLong("datelimite"); // Récupérer comme objet Long

                    if (datelimite != null && System.currentTimeMillis() >= datelimite) {
                        // Si la date limite est dépassée, supprimer la vente et remettre les produits disponibles
                        System.out.println("La date limite pour cette vente est dépassée. Suppression de la vente et remise des produits disponibles.");

                        // Supprimer les offres liées (pas nécessaire ici car aucune offre, mais par sécurité)
                        String sqlDeleteOffres = "DELETE FROM Offre WHERE idVente = ?";
                        PreparedStatement stmtDeleteOffres = conn.prepareStatement(sqlDeleteOffres);
                        stmtDeleteOffres.setInt(1, idVente);
                        stmtDeleteOffres.executeUpdate();

                        // Remettre le produit disponible
                        String sqlUpdateProduit = "UPDATE Produits SET disponible = 1 WHERE idProduit = (SELECT idProduit FROM Vente WHERE idVente = ?)";
                        PreparedStatement stmtUpdateProduit = conn.prepareStatement(sqlUpdateProduit);
                        stmtUpdateProduit.setInt(1, idVente);
                        stmtUpdateProduit.executeUpdate();

                        // Supprimer la vente
                        String sqlDeleteVente = "DELETE FROM Vente WHERE idVente = ?";
                        PreparedStatement stmtDeleteVente = conn.prepareStatement(sqlDeleteVente);
                        stmtDeleteVente.setInt(1, idVente);
                        stmtDeleteVente.executeUpdate();

                        // Commit des modifications
                        conn.commit();

                        System.out.println("La vente a été supprimée et les produits sont à nouveau disponibles.");
                    } else {
                        System.out.println("La date limite n'est pas atteinte ou la vente n'a pas de durée limitée.");
                    }
                
            }
        }
            }
            


    }
        }
    }
}

