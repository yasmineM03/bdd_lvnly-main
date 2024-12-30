import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

public class EnchereQuery {

    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lecontev";
    static final String PASSWD = "lecontev";

    private Connection conn;
    public EnchereQuery() {
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            conn.setAutoCommit(false); 
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données :");
            e.printStackTrace();
        }
    }

    public void createEnchere(Scanner scanner) throws Exception {
    System.out.println("Création d'une enchère :");

    // Récupérer les informations nécessaires
    String sqlGetMail = "SELECT DISTINCT email FROM utilisateur";
        PreparedStatement stmtMail = conn.prepareStatement(sqlGetMail);
        ResultSet resultMail = stmtMail.executeQuery();
        System.out.println("Les emails disponibles :");
        while (resultMail.next()){
            String mail = resultMail.getString("email");
            System.out.println(mail);
        }
    System.out.print("Entrez l'adresse email de l'utilisateur : ");
    String utilisateurMail = scanner.nextLine();

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


    // Vérifier que la vente existe
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
    
    int idProduit = rsCheck.getInt("idProduit");
    String sqlGetQuantite = "SELECT stock FROM produits WHERE idProduit = ?";
    PreparedStatement stmtQuantite = conn.prepareStatement(sqlGetQuantite);
    stmtQuantite.setInt(1, idProduit);
    ResultSet resultQuantite= stmtQuantite.executeQuery();
    System.out.println("La quantité disponible pour cette vente:");
    resultQuantite.next();
    String stock = resultQuantite.getString("stock");
    System.out.println(stock);
    String sqlDateVente = "SELECT datelimite FROM vente WHERE idVente = ?";
    PreparedStatement stmtDateVente = conn.prepareStatement(sqlDateVente);
    stmtDateVente.setInt(1, idVente);
    ResultSet resultDate = stmtDateVente.executeQuery();
    resultDate.next();
    long datelimite = resultDate.getLong("datelimite");
    if ( !resultDate.wasNull() && System.currentTimeMillis() >= datelimite){
        System.out.printf("La date limite pour soumettre une offre pour la vente %d est dépassée \n",idVente);
        return;
    }
     
    System.out.print("Entrez la quantité : ");
    int quantite = scanner.nextInt();
    scanner.nextLine();
    String sqlCroissance = "SELECT croissance FROM SalleDeVente WHERE idSalle = (SELECT idSalle FROM Vente WHERE idVente = ?)";
    PreparedStatement stmtCroissance = conn.prepareStatement(sqlCroissance);
    stmtCroissance.setInt(1, idVente);
    ResultSet rsCroissance = stmtCroissance.executeQuery();
    rsCroissance.next();
    int croissance = rsCroissance.getInt("croissance");
    double prixActuel = 0;
    double maxPrix;
    if (croissance == 1) {
        // Si l'enchère est descendante (croissance = 1), récupérer le prix actuel basé sur le temps
        prixActuel = getPrixActuelDescendant(idVente);
        System.out.println("Prix actuel (descendant) : " + prixActuel);
        maxPrix = prixActuel;
    }
    else {  
    // Récupérer le prix de départ de la vente
    String sqlPrixDepart = "SELECT prixdepart FROM Vente WHERE idVente = ?";
    PreparedStatement stmtDepart = conn.prepareStatement(sqlPrixDepart);
    stmtDepart.setInt(1, idVente);
    ResultSet rsDepart = stmtDepart.executeQuery();

    double prixDepart = 0;
    if (rsDepart.next()) {
        prixDepart = rsDepart.getDouble("prixdepart"); // Récupérer le prix de départ
    }

    // Vérifier s'il existe des offres pour obtenir le prix max
    String sqlMaxPrix = "SELECT MAX(prix) FROM Offre WHERE idVente = ?";
    PreparedStatement stmtMax = conn.prepareStatement(sqlMaxPrix);
    stmtMax.setInt(1, idVente);
    ResultSet rsMax = stmtMax.executeQuery();

    maxPrix = prixDepart; // On commence avec le prix de départ

    if (rsMax.next()) {
        double maxOffre = rsMax.getDouble(1); // Récupérer le prix max des offres existantes
        maxPrix = Math.max(prixDepart, maxOffre); // On prend le max entre prix de départ et prix max des offres
    }

    // Affichage du prix maximal actuellement
    System.out.println("Le prix de l'offre la plus haute actuellement (ou le prix de départ) : " + maxPrix);
}


    System.out.print("Entrez le prix de l'offre (prix affiché si la vente est descendante) : ");
    double prixOffre = scanner.nextDouble();
    scanner.nextLine();


 // Vérifier la validité de l'offre (prix max pour cette vente)
    if (quantite <= 0 || prixOffre <= 0) {
        throw new IllegalArgumentException("La quantité et le prix doivent être positifs !");
    }
    if (prixOffre <= maxPrix && croissance == 0) {
        System.out.println("Erreur : L'offre doit être supérieure à  " + maxPrix);
        return;
    }
    else if (prixOffre < maxPrix && croissance == 1) {
        System.out.println("Erreur : L'offre doit être supérieure à  " + maxPrix);
        return;
    }
    
    String sql = """
        INSERT INTO Offre (email, idVente, dateoffre, quantite, prix,time)
        VALUES (?, ?, ?, ?, ?,?)
    """;
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, utilisateurMail);
    stmt.setInt(2, idVente);
    stmt.setLong(3, System.currentTimeMillis());
    stmt.setInt(4, quantite);
    stmt.setDouble(5, prixOffre);
    stmt.setLong(6,System.currentTimeMillis());

    int rowsInserted = stmt.executeUpdate();
    if (rowsInserted > 0) {
        if(croissance == 1){
            System.out.println("Bravo !! Vous avez gagné l'enchère !");
            conn.commit();}
        else{  
        String sqlLastOfferTime = """
        SELECT MAX(time) AS lastOfferTime,email
        FROM Offre
        WHERE idVente = ?
        GROUP BY email
        """;
        PreparedStatement stmtLastOffer = conn.prepareStatement(sqlLastOfferTime);
        stmtLastOffer.setInt(1, idVente);
        ResultSet rsLastOffer = stmtLastOffer.executeQuery();
        rsLastOffer.next();
        long lastOfferTime = rsLastOffer.getLong("lastOfferTime");
        String emailGagnant = rsLastOffer.getString("email");
        if (System.currentTimeMillis() - lastOfferTime > 6000) {
            // Clôturer l'enchère
            System.out.println("10 minutes écoulée. L'enchère est terminée.");
        }
        else{ 
        conn.commit();
        System.out.println("Enchère ajoutée avec succès !");
        } 
        } 
    } else {
        System.out.println("Échec de l'ajout de l'enchère.");
    }
}


public double getPrixActuelDescendant(int idVente) throws SQLException {
    long currentTime = System.currentTimeMillis();
    String sqlVente = "SELECT dateDebut, prixdepart FROM Vente WHERE idVente = ?";
    PreparedStatement stmtVente = conn.prepareStatement(sqlVente);
    stmtVente.setInt(1, idVente);
    ResultSet rsVente = stmtVente.executeQuery();

    if (rsVente.next()){
        long dateDebut = rsVente.getLong("dateDebut");
        double prixDepart = rsVente.getDouble("prixDepart");
        long timeElapsed = currentTime - dateDebut;
        int interval = 6;
        int intervalCount = (int)(timeElapsed/(interval*1000));
        double prixActuel = prixDepart - (prixDepart*intervalCount*0.01);

        if (prixActuel < 0){
            prixActuel = 0;
        } 
        return prixActuel;

    }
    return 0;
}

}
