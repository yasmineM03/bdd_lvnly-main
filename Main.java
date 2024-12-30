import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        SalleDeVenteQuery query = new SalleDeVenteQuery();
        EnchereQuery query1 = new EnchereQuery();
        GagnantQuery query2 = new GagnantQuery();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMenu :");
            System.out.println("1. Créer une salle de vente");
            System.out.println("2. Ajouter une enchère");
            System.out.println("3. Rechercher un gagnant");
            System.out.println("4. Quitter");
            System.out.print("Choisissez une option : ");

            if (!scanner.hasNextInt()) {
                System.out.println("Veuillez entrer un entier valide (1-5) !");
                scanner.nextLine();
                continue;
            }

            int choix = scanner.nextInt();
            scanner.nextLine(); 
            switch (choix) {
                case 1:
                    query.createSalleDeVente(scanner);
                    break;
                case 2:
                    query1.createEnchere(scanner);
                    break;
                case 3:
                    query2.chercheGagnant(scanner);
                    break;
                case 4:
                    System.out.println("Au revoir !");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
                    break;
            }
        }
    }
}
