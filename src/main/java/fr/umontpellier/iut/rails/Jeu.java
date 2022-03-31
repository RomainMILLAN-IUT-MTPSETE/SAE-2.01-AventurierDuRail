package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private List<Joueur> joueurs;
    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes représentées sur le plateau de jeu
     */
    private List<Ville> villes;
    /**
     * Liste des routes du plateau de jeu
     */
    private List<Route> routes;
    /**
     * Pile de pioche (face cachée)
     */
    private List<CouleurWagon> pileCartesWagon;
    /**
     * Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
     */
    private List<CouleurWagon> cartesWagonVisibles;
    /**
     * Pile de cartes qui ont été défaussée au cours de la partie
     */
    private List<CouleurWagon> defausseCartesWagon;
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private List<String> log;
    /**
     * Pile destination long
     */
    private List<Destination> longDestinationList;

    public Jeu(String[] nomJoueurs) {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         * 
         * Le code indiqué ici est un squelette minimum pour que le jeu se lance et que
         * l'interface graphique fonctionne.
         * Vous devez modifier ce code pour que les différents éléments du jeu soient
         * correctement initialisés.
         */

        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des cartes
        pileCartesWagon = new ArrayList<>();
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        pileDestinations = new ArrayList<>();

        // création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));
            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();
        //PERSO
        this.longDestinationList = Destination.makeDestinationsLonguesEurope();
        this.pileDestinations = Destination.makeDestinationsEurope();

        //MISE EN PLACE DES CARTES WAGON VISIBLES
        for(int i=0; i<12; i++){
            this.pileCartesWagon.add(CouleurWagon.ROSE);
            this.pileCartesWagon.add(CouleurWagon.BLANC);
            this.pileCartesWagon.add(CouleurWagon.BLEU);
            this.pileCartesWagon.add(CouleurWagon.JAUNE);
            this.pileCartesWagon.add(CouleurWagon.ORANGE);
            this.pileCartesWagon.add(CouleurWagon.NOIR);
            this.pileCartesWagon.add(CouleurWagon.ROUGE);
            this.pileCartesWagon.add(CouleurWagon.VERT);
        }

        for(int i=0; i<14; i++){
            this.pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        }

        Collections.shuffle(this.pileCartesWagon);

        for(int i=0; i<this.joueurs.size(); i++){
            for(int j=0; j<4; j++){
                this.joueurs.get(i).getCartesWagon().add(this.piocherCarteWagon());
            }
        }

        this.resetCarteWagonVisible();
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Destination> getPileDestinations() { return pileDestinations; }

    public List<CouleurWagon> getDefausseCartesWagon() { return defausseCartesWagon; }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    /**
     * Exécute la partie
     */
    public void run() {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         * 
         * Cette méthode doit :
         * - faire choisir à chaque joueur les destinations initiales qu'il souhaite
         * garder : on pioche 3 destinations "courtes" et 1 destination "longue", puis
         * le
         * joueur peut choisir des destinations à défausser ou passer s'il ne veut plus
         * en défausser. Il doit en garder au moins 2.
         * - exécuter la boucle principale du jeu qui fait jouer le tour de chaque
         * joueur à tour de rôle jusqu'à ce qu'un des joueurs n'ait plus que 2 wagons ou
         * moins
         * - exécuter encore un dernier tour de jeu pour chaque joueur après
         */

        /**
         * PERSONNEL
         */
        //DEBUT DU JEU, SELECTION DES CARTES
        for(int i=0; i<joueurs.size(); i++){
            this.joueurCourant = this.joueurs.get(i);
            ArrayList<Destination> destinationPlayer = new ArrayList<>();
            destinationPlayer.add(this.getRandomLongDestinationCard());

            //On pioche au hasard 3 carte et on les mets dans la liste en cour.
            ArrayList<Destination> destinationToCopieCardNormal = this.getRandomDestinationCard(3);
            for(int j=0; j<destinationToCopieCardNormal.size(); j++){
                destinationPlayer.add(destinationToCopieCardNormal.get(j));
            }

            List<Destination> aSupprimer = joueurs.get(i).choisirDestinations(destinationPlayer, 2);
        }

        //LANCEMENT DU JEU
        boolean arretDuJeu = false;
        int dernierTourDujeu = 0;
        while(arretDuJeu == false || dernierTourDujeu < joueurs.size()){
            for(int i=0; i<joueurs.size(); i++){
                log("Au tour de <strong>" + joueurs.get(i).getNom() + "</strong>");
                this.joueurCourant = this.joueurs.get(i);

                //Lancement du tour du joueur, voir sa suite dans Joueur.java
                this.joueurCourant.jouerTour();


                //Pour la fin du jeu.
                if(arretDuJeu == true){
                    dernierTourDujeu++;
                }
                //Si le joueur à moin de 3 wagon (0,1 ou 2), alors la fin du jeu commence.
                if(joueurs.get(i).getNbWagons() < 3){
                    arretDuJeu = true;
                }
            }
        }


        /**
         * Le code proposé ici n'est qu'un exemple d'utilisation des méthodes pour
         * interagir avec l'utilisateur, il n'a rien à voir avec le code de la partie et
         * doit donc être entièrement réécrit.
         */
        /*
        // Exemple d'utilisation
        while (true) {
            // le joueur doit choisir une valeur parmi "1", "2", "3", "4", "6" ou "8"
            // les choix possibles sont présentés sous forme de boutons cliquables
            String choix = joueurCourant.choisir(
                    "Choisissez une taille de route.", // instruction
                    new ArrayList<>(), // choix (hors boutons, ici aucun)
                    new ArrayList<>(Arrays.asList("1", "2", "3", "4", "6", "8")), // boutons
                    false); // le joueur ne peut pas passer (il doit faire un choix)

            // une fois la longueur choisie, on filtre les routes pour ne garder que les
            // routes de la longueur choisie
            int longueurRoute = Integer.parseInt(choix);
            ArrayList<String> routesPossibles = new ArrayList<>();
            for (Route route : routes) {
                if (route.getLongueur() == longueurRoute) {
                    routesPossibles.add(route.getNom());
                }
            }

            // le joueur doit maintenant choisir une route de la longueur choisie (les
            // autres ne sont pas acceptées). Le joueur peut choisir de passer (aucun choix)
            String choixRoute = joueurCourant.choisir(
                    "Choisissez une route de longueur " + longueurRoute, // instruction
                    routesPossibles, // choix (pas des boutons, il faut cliquer sur la carte)
                    new ArrayList<>(), // boutons (ici aucun bouton créé)
                    true); // le joueur peut passer sans faire de choix
            if (choixRoute.equals("")) {
                // le joueur n'a pas fait de choix (cliqué sur le bouton "passer")
                log("Auncune route n'a été choisie");
            } else {
                // le joueur a choisi une route
                log("Vous avez choisi la route " + choixRoute);
            }
        }*/
    }

    /**
     * Ajoute une carte dans la pile de défausse.
     * Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
     * (parce que la pioche
     * et la défausse sont vides), alors il faut immédiatement rendre cette carte
     * face visible.
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {

        if(cartesWagonVisibles.size()<5){
            cartesWagonVisibles.add(c);
        }

        defausseCartesWagon.add(c);
    }

    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {
        CouleurWagon cartePiocher = null;
        boolean defausseEmpty = false;

        if(pileCartesWagon.isEmpty()){
            if(this.defausseCartesWagon.isEmpty()){
                defausseEmpty = true;
            }else {
                //Copie de la défause dans la pile & suppression des cartes de la défausse.
                for(int i=0; i<this.defausseCartesWagon.size(); i++){
                    this.pileCartesWagon.add(this.defausseCartesWagon.get(0));
                    this.defausseCartesWagon.remove(0);
                }

                //On mélange les cartes dans la pile.
                Collections.shuffle(this.pileCartesWagon);
            }

        }

        if(defausseEmpty == false){
            cartePiocher = this.pileCartesWagon.get(0);
            this.pileCartesWagon.remove(0);
        }


        return cartePiocher;
    }

    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c){
        this.cartesWagonVisibles.remove(c);

        //J'utilise une autre fonction que j'avais crée avant pour regarder si il y a + de 3 locomotive et ajouter le reste des cartes
        this.resetCarteWagonVisible();

    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     * 
     * @return la destination qui a été piochée (ou `null` si aucune destination
     *         disponible)
     */
    public Destination piocherDestination() {
        // throw new RuntimeException("Méthode non implémentée !");
        Destination d = null;

        if(pileDestinations.isEmpty()){
            d = null;
        }else {
            d = pileDestinations.get(0);
            this.pileDestinations.remove(0);
        }

        return d;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     *         file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }


    /**
     * PERSONEL
     */
    /**
     * Cette fonction permet d'avoir une carte longue distance en random, celle-ci sera retournée a la fin de l'execution.
     * @return
     */
    public Destination getRandomLongDestinationCard(){
        Destination resultat;

        //On fait un random pour avoir l'id d'une destination
        Random rand = new Random();
        int randomNumber = rand.nextInt(this.longDestinationList.size());

        //Puis on la get dans la liste
        resultat = this.longDestinationList.get(randomNumber);
        this.longDestinationList.remove(randomNumber);

        //Et enfin on la retourne.
        return resultat;
    }

    /**
     * Retourne une liste de carte destination piocher au hasard dans la pileDestination.
     * @param numberCardToGet
     * @return
     */
    public ArrayList<Destination> getRandomDestinationCard(int numberCardToGet){
        ArrayList<Destination> resultat = new ArrayList<>();

        //Puis on pioche dedans
        for(int i=0; i<numberCardToGet; i++){
            Random rand = new Random();
            int randomNumber = rand.nextInt(this.pileDestinations.size());

            resultat.add(this.pileDestinations.get(randomNumber));
            this.pileDestinations.remove(randomNumber);
        }

        return resultat;
    }

    /**
     * Remet toutes les cartes wagon en face visible (5)
     */
    public void resetCarteWagonVisible(){
        int carteWagonVisibleSizeFirst = this.cartesWagonVisibles.size();
        if(this.cartesWagonVisibles.size() < 5){
            for(int i=0; i<5-carteWagonVisibleSizeFirst; i++){
                CouleurWagon select = this.piocherCarteWagon();
                this.cartesWagonVisibles.add(select);
            }
        }

        int nbrCarteLoco = 0;
        for(int i=0; i<this.cartesWagonVisibles.size(); i++){
            if(this.cartesWagonVisibles.get(i).equals(CouleurWagon.LOCOMOTIVE)){
                nbrCarteLoco++;
            }
        }

        if(nbrCarteLoco >= 3){
            this.resetAllCarteWagonVisible();
        }else {
            log("Remise de " + (5-carteWagonVisibleSizeFirst) + " cartes en carte <strong>visibles</strong>.");
        }
    }

    /**
     * Remet 5 nouvelles cartes dans les wagon visibles
     */
    public void resetAllCarteWagonVisible(){
        int carteWagonVisibleSizeFirst = this.cartesWagonVisibles.size();
        for(int i=0; i<carteWagonVisibleSizeFirst; i++){
            this.defausseCartesWagon.add(this.cartesWagonVisibles.get(0));
            this.cartesWagonVisibles.remove(0);
        }

        for(int i=0; i<5; i++){
            CouleurWagon select = piocherCarteWagon();
            this.cartesWagonVisibles.add(select);
        }

        int nbrCarteLoco = 0;
        for(int i=0; i<this.cartesWagonVisibles.size(); i++){
            if(this.cartesWagonVisibles.get(i).equals(CouleurWagon.LOCOMOTIVE)){
                nbrCarteLoco++;
            }
        }

        if(nbrCarteLoco >= 3){
            this.resetAllCarteWagonVisible();
        }else {
            log("Reset de <strong>TOUTES</strong> les cartes wagon visibles.");
        }
    }

    /**
     * Fonction qui permet de piocher des Destinations dans jouerTour()
     */
    public void jouerTourPiocherDestination(){
        //piocher 3 et en garder au moins 1 et défausser 2 max pas obliger donc fin liste
        ArrayList<Destination> res = new ArrayList<Destination>();

        res.add(this.piocherDestination());
        res.add(this.piocherDestination());
        res.add(this.piocherDestination());

        ArrayList<Destination> finalRes = this.joueurCourant.choisirDestination(res);
        this.joueurCourant.addDestinationListCardToListPlayer(finalRes);
        for(Destination first : res){
            if(!finalRes.contains(first)){
                this.pileDestinations.add(first);
            }
        }
        log("<strong>" + this.joueurCourant.getNom() + "</strong>, à piocher " + finalRes.size() + " cartes destinations");
    }

    public static String DEVPREFIX = "<strong><p style='color: red'>MILLANR-TREGUIERE/DEVELOPPEMENT</p></strong> ";
}
