package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        List<Destination> resultatCardToDefausser = new ArrayList<>();
        int defausser = 0;
        boolean passe = false;
        while(defausser < n && passe == false){
            List<String> boutons = new ArrayList<>();
            for(int j=0; j<destinationsPossibles.size(); j++){
                boutons.add(destinationsPossibles.get(j).getNom());
            }

            System.out.println(this.nom);
            String input = choisir("Choisissez une carte à défausser.", new ArrayList<>(), boutons, true);

            if(input.equals("")){
                //Le joueur garde toutes ces cartes
                passe = true;
            }else {
                //Le joueur défausse une carte
                defausser++;
                int i=0;
                boolean find = false;
                while(i<destinationsPossibles.size() && find == false){
                    if(destinationsPossibles.get(i).getNom().equals(input)){
                        find = true;
                        resultatCardToDefausser.add(destinationsPossibles.get(i));
                        destinationsPossibles.remove(i);
                    }
                    i++;
                }
            }


        }

        //Ajout des cartes destinations non défausser dans le jeu du joueur
        ArrayList<Destination> copieRes = new ArrayList();
        for(int i=0; i<resultatCardToDefausser.size(); i++){
            copieRes.add(resultatCardToDefausser.get(i));
        }

        for(int i=0; i<destinationsPossibles.size(); i++){
            this.destinations.add(destinationsPossibles.get(i));
        }



        return resultatCardToDefausser;
    }

    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {


        ArrayList<String> others = new ArrayList();
        others.add(CouleurWagon.GRIS.toString().toUpperCase());
        others.add(CouleurWagon.LOCOMOTIVE.toString().toUpperCase());
        others.add(CouleurWagon.ROSE.toString().toUpperCase());
        others.add(CouleurWagon.BLANC.toString().toUpperCase());
        others.add(CouleurWagon.BLEU.toString().toUpperCase());
        others.add(CouleurWagon.JAUNE.toString().toUpperCase());
        others.add(CouleurWagon.ORANGE.toString().toUpperCase());
        others.add(CouleurWagon.NOIR.toString().toUpperCase());
        others.add(CouleurWagon.ROUGE.toString().toUpperCase());
        others.add(CouleurWagon.VERT.toString().toUpperCase());
        others.add("destinations");
        for(int i=0; i<this.jeu.getVilles().size(); i++){
            others.add(this.jeu.getVilles().get(i).getNom());
        }
        for(int i=0; i<this.jeu.getRoutes().size(); i++){
            others.add(this.jeu.getRoutes().get(i).getNom());
        }
        String choix = this.choisir(
                "Choissisez l'action à effectué.", // instruction
                others, // choix (hors boutons, ici aucun)
                new ArrayList<>(),
                false); // le joueur ne peut pas passer (il doit faire un choix)

        /*if(choix.equalsIgnoreCase("Piocher une carte Wagon (Visible)")){
            this.jeu.jouerTourPiocheWagonVisible();
        }else if(choix.equalsIgnoreCase("Piocher une carte Wagon (Cachée)")){
            this.jeu.jouerTourPiocherWagon();
        }else if(choix.equalsIgnoreCase("Piocher une Destination")){
            this.jeu.jouerTourPiocherDestination();
        }else if (choix.equalsIgnoreCase("Poser une Gare")){
            this.jouerTourPoserGare();
        }else if(choix.equalsIgnoreCase("Poser une Route")){
            this.nbGares = 1;
            this.jeu.jouerTourPoserRoute();
        }else {
            System.out.println("ERROR");
        }*/

        ArrayList<String> wagonVisibleSelect = new ArrayList();
        wagonVisibleSelect.add(CouleurWagon.LOCOMOTIVE.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.ROSE.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.BLANC.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.BLEU.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.JAUNE.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.ORANGE.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.NOIR.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.ROUGE.toString().toUpperCase());
        wagonVisibleSelect.add(CouleurWagon.VERT.toString().toUpperCase());

        ArrayList<String> villesSelect = new ArrayList<>();
        for(int i=0; i<this.jeu.getVilles().size(); i++){
            villesSelect.add(this.jeu.getVilles().get(i).getNom());
        }

        ArrayList<String> routeSelect = new ArrayList<>();
        for(int i=0; i<this.jeu.getRoutes().size(); i++){
            routeSelect.add(this.jeu.getRoutes().get(i).getNom());
        }





        //CHOISI: DESTINATION
        if(choix.equalsIgnoreCase("destinations")){
            this.jeu.jouerTourPiocherDestination();
            this.jeu.log("<strong>" + this.getNom() + "</strong>, à piocher des cartes destinations.");
        }else if(wagonVisibleSelect.contains(choix)){
            //CHOISI: WAGON VISIBLE
            for(int i=0; i<this.jeu.getCartesWagonVisibles().size(); i++){
                if(this.jeu.getCartesWagonVisibles().get(i).toString().toUpperCase().equalsIgnoreCase(choix)){
                    this.cartesWagon.add(this.jeu.getCartesWagonVisibles().get(i));
                    this.jeu.retirerCarteWagonVisible(this.jeu.getCartesWagonVisibles().get(i));
                    break;
                }
            }
            if(!choix.equalsIgnoreCase("Locomotive")){
                List<String> boutons = new ArrayList<>();
                ArrayList<CouleurWagon> resSelect = new ArrayList<>();
                boolean stop = false;
                for(int i=0; i<this.jeu.getCartesWagonVisibles().size(); i++){
                    boutons.add(this.jeu.getCartesWagonVisibles().get(i).toString().toUpperCase());
                }

                do{
                    choix = choisir("Choisissez une carte wagon.", boutons, new ArrayList<>(), true);
                }while(choix.equalsIgnoreCase("Locomotive".toUpperCase()));

                if(!choix.equalsIgnoreCase("")){
                    for(int i=0; i<this.jeu.getCartesWagonVisibles().size(); i++){
                        if(this.jeu.getCartesWagonVisibles().get(i).toString().toUpperCase().equalsIgnoreCase(choix)){
                            this.cartesWagon.add(this.jeu.getCartesWagonVisibles().get(i));
                            this.jeu.retirerCarteWagonVisible(this.jeu.getCartesWagonVisibles().get(i));
                            break;
                        }
                    }
                }

            }
            this.jeu.resetCarteWagonVisible();
            this.jeu.log("<strong>" + this.getNom() + "</strong>, à piocher des wagons.");
        }else if(choix.equalsIgnoreCase(CouleurWagon.GRIS.toString())){
            //CHOISI: WAGON NON VISIBLE
            CouleurWagon w1 = this.jeu.piocherCarteWagon();
            CouleurWagon w2 = this.jeu.piocherCarteWagon();
            this.ajouterCarteWagonDansMainJoueur(w1);
            this.ajouterCarteWagonDansMainJoueur(w2);

            this.jeu.log("<strong>" + this.getNom() + "</strong>, à piocher 2 cartes wagons.");
        }else if(villesSelect.contains(choix)){
            //CHOISI: GARE
            Ville villeChoisis = null;
            for(int i=0; i<this.jeu.getVilles().size(); i++){
                if(this.jeu.getVilles().get(i).toString().equalsIgnoreCase(choix)){
                    villeChoisis = this.jeu.getVilles().get(i);
                }
            }

            if(villeChoisis.getProprietaire() != null){
                this.jouerTour();
            }else {
                if(this.nbGares == 3){
                    //1er Gare
                    if(this.nbWagonMemeCouleurMax() >= 1){
                        ArrayList<String> cartesPossibles = new ArrayList<>();
                        for(int i=0; i<this.cartesWagon.size(); i++){
                            cartesPossibles.add(this.cartesWagon.get(i).toString());
                        }
                        String choixCarte = this.choisir("Choisir la carte à défausser", new ArrayList<>(), cartesPossibles, false);

                        for(int i=0; i<this.cartesWagon.size(); i++){
                            if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte)){
                                this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                this.cartesWagon.remove(i);
                                break;
                            }
                        }

                        ArrayList<String> finalChose = new ArrayList<>();
                        finalChose.add("Oui"); finalChose.add("Non, tous annulé");
                        String choixFinal = this.choisir("Êtes-vous sûre de vouloir prendre la gâre de " + villeChoisis.getNom() + ", avec la carte " + this.cartesWagonPosees.get(0).toString() + " ?", new ArrayList<>(), finalChose, false);

                        if(choixFinal.equalsIgnoreCase("Oui")){
                            villeChoisis.setProprietaire(this);
                            this.cartesWagonPosees.remove(0);
                            this.nbGares--;
                        }else {
                            this.cartesWagon.add(this.cartesWagonPosees.get(0));
                            this.cartesWagonPosees.remove(0);
                            this.jouerTour();
                        }

                    }else {
                        log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, vous n'avez pas assez de cartes wagons de même couleur");
                        this.jouerTour();
                    }
                }else if(this.nbGares == 2){
                    //2nd Gare => 2 wagon de mm couleur
                    if(this.nbWagonMemeCouleurMax() >= 2){

                        ArrayList<String> cartesPossibles = new ArrayList<>();
                        ArrayList<String> cartesSaves = new ArrayList<>();
                        for(int i=0; i<this.cartesWagon.size(); i++){
                            cartesPossibles.add(this.cartesWagon.get(i).toString());
                            cartesSaves.add(this.cartesWagon.get(i).toString());
                        }

                        String choixCarte1;
                        String choixCarte2;

                        if(this.nbWagonMemeCouleurMax() == 2 && this.cartesWagon.size() == 2){
                            choixCarte1 = this.cartesWagon.get(0).toString();
                            choixCarte2 = this.cartesWagon.get(1).toString();
                        }else {
                            do{
                                choixCarte1 = this.choisir("Choisir la 1er carte à défausser", new ArrayList<>(), cartesPossibles, false);
                                for(int i=0; i<cartesPossibles.size(); i++){
                                    if(cartesPossibles.get(i).toString().equalsIgnoreCase(choixCarte1)){
                                        cartesPossibles.remove(i);
                                        break;
                                    }
                                }
                                choixCarte2 = this.choisir("Choisir la 2nd carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                for(int i=0; i<cartesPossibles.size(); i++){
                                    cartesPossibles.remove(0);
                                }
                                cartesPossibles.addAll(cartesSaves);
                            }while(!choixCarte1.equalsIgnoreCase(choixCarte2) && !choixCarte1.equals(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte2.equals(CouleurWagon.LOCOMOTIVE.toString()));
                        }


                        boolean find = false;
                        int i=0;
                        while(i<this.cartesWagon.size()){
                            if(choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte2) || this.cartesWagon.get(i).toString().equals(CouleurWagon.LOCOMOTIVE.toString())){
                                    this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                    this.cartesWagon.remove(i);

                                    if(find == true){
                                        break;
                                    }else {
                                        find = true;
                                        i=0;
                                    }
                                }else {
                                    i++;
                                }
                            }else {
                                if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte1) || this.cartesWagon.get(i).toString().equals(CouleurWagon.LOCOMOTIVE.toString())){
                                    this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                    this.cartesWagon.remove(i);

                                    if(find == true){
                                        break;
                                    }else {
                                        find = true;
                                        i=0;
                                    }
                                }else {
                                    i++;
                                }
                            }

                        }

                        ArrayList<String> finalChose = new ArrayList<>();
                        finalChose.add("Oui"); finalChose.add("Non, tous annulé");
                        String choixFinal = this.choisir("Êtes-vous sûre de vouloir prendre la gâre de " + villeChoisis.getNom() + ", avec les cartes de couleurs " + this.cartesWagonPosees.get(0).toString() + " ?", new ArrayList<>(), finalChose, false);

                        if(choixFinal.equalsIgnoreCase("Oui")){
                            villeChoisis.setProprietaire(this);
                            this.cartesWagonPosees.remove(1);
                            this.cartesWagonPosees.remove(0);
                            this.nbGares--;
                        }else {
                            this.cartesWagon.add(this.cartesWagonPosees.get(0));
                            this.cartesWagon.add(this.cartesWagonPosees.get(1));
                            this.cartesWagonPosees.remove(0);
                            this.cartesWagonPosees.remove(0);
                            this.jouerTour();
                        }

                    }else {
                        log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, vous n'avez pas assez de cartes wagons de même couleur");
                        this.jouerTour();
                    }
                }else if(this.nbGares == 1){

                    ArrayList<String> cartesPossibles = new ArrayList<>();
                    ArrayList<String> cartesSaves = new ArrayList<>();
                    for(int i=0; i<this.cartesWagon.size(); i++){
                        cartesPossibles.add(this.cartesWagon.get(i).toString());
                        cartesSaves.add(this.cartesWagon.get(i).toString());
                    }

                    String choixCarte1;
                    String choixCarte2;
                    String choixCarte3;
                    boolean color1 = false;
                    boolean color2 = false;
                    boolean c1 = false;
                    boolean c2 = false;
                    boolean c3 = false;

                    if(this.nbWagonMemeCouleurMax() == 3 && this.cartesWagon.size() == 3){
                        choixCarte1 = this.cartesWagon.get(0).toString();
                        choixCarte2 = this.cartesWagon.get(1).toString();
                        choixCarte3 = this.cartesWagon.get(2).toString();
                    }else {
                        do{
                            color1 = false;
                            color2 = false;
                            c1 = false;
                            c2 = false;
                            c3 = false;
                            cartesPossibles = new ArrayList<>();
                            for(int i=0; i<this.cartesWagon.size(); i++){
                                cartesPossibles.add(this.cartesWagon.get(i).toString());
                                cartesSaves.add(this.cartesWagon.get(i).toString());
                            }
                            choixCarte1 = this.choisir("Choisir la 1er carte à défausser", new ArrayList<>(), cartesPossibles, false);

                            if(choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                c1 = true;
                            }else {
                                c1 = true;
                                color1 = true;
                            }

                            for(int i=0; i<cartesPossibles.size(); i++){
                                if(cartesPossibles.get(i).toString().equalsIgnoreCase(choixCarte1)){
                                    cartesPossibles.remove(i);
                                    break;
                                }
                            }
                            choixCarte2 = this.choisir("Choisir la 2nd carte à défausser", new ArrayList<>(), cartesPossibles, false);
                            if(color1 == true){
                                if(choixCarte2.equalsIgnoreCase(choixCarte1) || choixCarte2.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                    c2 = true;
                                }
                            }else {
                                if(!choixCarte2.equalsIgnoreCase("Locomotive")){
                                    color2 = true;
                                }
                                c2=true;
                            }
                            for(int i=0; i<cartesPossibles.size(); i++){
                                if(cartesPossibles.get(i).toString().equalsIgnoreCase(choixCarte2)){
                                    cartesPossibles.remove(i);
                                    break;
                                }
                            }
                            choixCarte3 = this.choisir("Choisir la 3eme carte à défausser", new ArrayList<>(), cartesPossibles, false);
                            if(color1 == true){
                                if(choixCarte3.equalsIgnoreCase(choixCarte1) || choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                    c3 = true;
                                }
                            }else if(color2 == true){
                                if(choixCarte3.equalsIgnoreCase(choixCarte2) || choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                    c3 = true;
                                }
                            }else {
                                c3 = true;
                            }
                            cartesPossibles.removeAll(cartesPossibles);
                        }while(c1!=true || c2!=true || c3!=true);
                        //while((!choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte2.equalsIgnoreCase(choixCarte3)) && (!choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte2.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())) && (!choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte2.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())) && (!choixCarte1.equalsIgnoreCase(choixCarte3) && !choixCarte2.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())) && (!choixCarte2.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())) && (!choixCarte1.equalsIgnoreCase(choixCarte2) && !choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())) && (!choixCarte1.equalsIgnoreCase(choixCarte2) && !choixCarte2.equalsIgnoreCase(choixCarte3)) && (!choixCarte1.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixCarte3.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())));
                    }

                    for(int i=0; i<this.cartesWagon.size(); i++){
                        if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte1)){
                            this.cartesWagonPosees.add(this.cartesWagon.get(i));
                            this.cartesWagon.remove(i);
                            break;
                        }
                    }
                    for(int i=0; i<this.cartesWagon.size(); i++){
                        if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte2)){
                            this.cartesWagonPosees.add(this.cartesWagon.get(i));
                            this.cartesWagon.remove(i);
                            break;
                        }
                    }
                    for(int i=0; i<this.cartesWagon.size(); i++){
                        if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte3)){
                            this.cartesWagonPosees.add(this.cartesWagon.get(i));
                            this.cartesWagon.remove(i);
                            break;
                        }
                    }

                    ArrayList<String> finalChose = new ArrayList<>();
                    finalChose.add("Oui"); finalChose.add("Non, tous annulé");
                    String choixFinal = this.choisir("Êtes-vous sûre de vouloir prendre la gâre de " + villeChoisis.getNom() + ", avec les cartes de couleurs " + this.cartesWagonPosees.get(0).toString() + " ?", new ArrayList<>(), finalChose, false);

                    if(choixFinal.equalsIgnoreCase("Oui")){
                        villeChoisis.setProprietaire(this);
                        this.cartesWagonPosees.remove(2);
                        this.cartesWagonPosees.remove(1);
                        this.cartesWagonPosees.remove(0);
                        this.nbGares--;
                    }else {
                        this.cartesWagon.add(this.cartesWagonPosees.get(0));
                        this.cartesWagon.add(this.cartesWagonPosees.get(1));
                        this.cartesWagon.add(this.cartesWagonPosees.get(2));
                        this.cartesWagonPosees.remove(2);
                        this.cartesWagonPosees.remove(1);
                        this.cartesWagonPosees.remove(0);
                        this.jouerTour();
                    }


                    this.nbGares = 1;

                }else {
                    log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, nombre de gares incompris.");
                    this.jouerTour();
                }
            }
        }else if(routeSelect.contains(choix)){
            //CHOISI: ROUTE
            Route routeChoisi = null;

            for(int i=0; i<this.jeu.getRoutes().size(); i++){
                if(this.jeu.getRoutes().get(i).getNom().equalsIgnoreCase(choix)){
                    routeChoisi = this.jeu.getRoutes().get(i);
                    break;
                }
            }

            if(routeChoisi.getCouleur().equals(CouleurWagon.GRIS)){
                if(this.cartesWagon.size() >= routeChoisi.getLongueur()){

                    if(routeChoisi.estFerry() == true){

                    }else if(routeChoisi.estTunnel() == true){

                    }else {
                        int x = 0;

                        ArrayList<String> listWagonJoueur = new ArrayList();
                        ArrayList<CouleurWagon> toCheck = new ArrayList<>();
                        for(int i=0; i<this.cartesWagon.size(); i++){
                            listWagonJoueur.add(this.cartesWagon.get(i).toString());
                        }

                        while(x < routeChoisi.getLongueur()){
                            String choixWagons = this.choisir("Choissisez les cartes à défausser.", new ArrayList<>(), listWagonJoueur,false);

                            for(int i=0; i<listWagonJoueur.size(); i++){
                                if(listWagonJoueur.get(i).equalsIgnoreCase(choixWagons)){
                                    listWagonJoueur.remove(i);
                                    break;
                                }
                            }

                            for(int i=0; i<this.cartesWagon.size(); i++){
                                if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixWagons)){
                                    toCheck.add(this.cartesWagon.get(i));
                                    break;
                                }
                            }

                            x++;
                        }

                        if(x == routeChoisi.getLongueur()){
                            int k=0;
                            for(int i=0; i<toCheck.size(); i++){
                                int j=0;
                                boolean find=false;

                                while(j<this.cartesWagon.size() && find==false){
                                    if(this.cartesWagon.get(j).equals(toCheck.get(i))){
                                        this.jeu.defausserCarteWagon(this.cartesWagon.get(j));
                                        this.cartesWagon.remove(j);
                                        find = true;
                                    }
                                }
                            }

                            routeChoisi.setProprietaire(this);
                        }else {
                            log("ERREUR, nombre de carte invalide");
                            this.jouerTour();
                        }
                    }

                    this.addScoreEnFonctionDeRoute(routeChoisi);
                }else {
                    this.jeu.log("ERREUR, nombre de wagon invalide !");
                    this.jouerTour();
                }
            }else {
                int nbLoco = this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE);

                int choixLocoNb = 0;
                if(nbLoco > 0){
                    ArrayList<String> nbDeLoco = new ArrayList<>();
                    nbDeLoco.add("0");
                    for(int i=1; i<=nbLoco; i++){
                        nbDeLoco.add(String.valueOf(i));
                    }
                    choixLocoNb = Integer.parseInt(this.choisir("Chossisez le nombre de Locomotive à défausser.", new ArrayList<>(), nbDeLoco,false));
                }
                if(routeChoisi.getLongueur()-choixLocoNb <= this.getNbWagonByCoul(routeChoisi.getCouleur())){
                    if(routeChoisi.estFerry() == true){

                    }else if(routeChoisi.estTunnel() == true){

                    }else {
                        //ROUTE NORMALE
                        routeChoisi.setProprietaire(this);
                        int nbWagonCoul = routeChoisi.getLongueur() - choixLocoNb;
                        int x = 0;
                        int comptCoulWagon = 0;
                        int comptCoulLoco = 0;
                        while(x < this.cartesWagon.size() && comptCoulWagon<nbWagonCoul){

                            if(this.cartesWagon.get(x).equals(routeChoisi.getCouleur())){
                                this.cartesWagon.remove(x);
                                comptCoulWagon++;
                                x=0;
                            }

                            if(this.cartesWagon.get(x).equals(CouleurWagon.LOCOMOTIVE)){
                                this.cartesWagon.remove(x);
                                comptCoulLoco++;
                                x=0;
                            }

                            x++;
                        }

                    }

                    this.addScoreEnFonctionDeRoute(routeChoisi);
                }else {
                    this.jeu.log("ERREUR, nombre de wagon invalide !");
                    this.jouerTour();
                }
            }



        }


    }

    /**
     * PERSONNEL
     */
    public void addScoreEnFonctionDeRoute(Route routeChosis){
        if(routeChosis.getLongueur() == 1){
            this.score += 1;
        }else if(routeChosis.getLongueur() == 2){
            this.score += 2;
        }else if(routeChosis.getLongueur() == 3){
            this.score += 4;
        }else if(routeChosis.getLongueur() == 4){
            this.score += 7;
        }else if(routeChosis.getLongueur() == 5){
            this.score += 15;
        }else if(routeChosis.getLongueur() == 6){
            this.score += 21;
        }
    }

    public void addDestinationListCardToListPlayer(List<Destination> destination){
        for(int i=0; i<destination.size(); i++){
            this.destinations.add(destination.get(i));
        }
    }

    public void addDestinationCardToListPlayer(Destination destination){
        this.destinations.add(destination);
    }

    public int getNbWagonByCoul(CouleurWagon coul){
        int res = 0;

        for(int i=0; i<this.cartesWagon.size(); i++){
            if(this.cartesWagon.get(i).toString().equalsIgnoreCase(coul.toString())){
                res++;
            }
        }

        return res;
    }

    /*public ArrayList<CouleurWagon> choisirCarteWagonVisible(List<CouleurWagon> listWagonVisible){
        ArrayList<CouleurWagon> resultat = new ArrayList<>();



        String input = choisir("Choisissez une carte wagon.", new ArrayList<>(), boutons, false);

        for(int i=0; i<listWagonVisible.size(); i++){
            if(listWagonVisible.get(i).toString().equalsIgnoreCase(input)){
                resultat.add(listWagonVisible.get(i));
                break;
            }
        }



        return resultat;
    }*/

    public void ajouterCarteWagonDansMainJoueur(CouleurWagon wagonColor){
        this.cartesWagon.add(wagonColor);
        this.nbWagons++;
    }

    public ArrayList<Destination> choisirDestination(ArrayList<Destination> listDestination){
        ArrayList<Destination> resultat = new ArrayList<>();

        List<String> boutons = new ArrayList<>();
        for(int i=0; i<listDestination.size(); i++){
            boutons.add(listDestination.get(i).getNom());
            resultat.add(listDestination.get(i));
        }

        String input = choisir("Vous pouvez défausser au maximum 2 cartes", new ArrayList<>(), boutons, true);

        if(!input.equalsIgnoreCase("")){
            for(int i=0; i<resultat.size(); i++){
                if(resultat.get(i).getNom().equalsIgnoreCase(input)){
                    resultat.remove(i);
                    break;
                }
            }

            for(int i=0; i<boutons.size(); i++){
                if(boutons.get(i).equalsIgnoreCase(input)){
                    boutons.remove(i);
                    break;
                }
            }

            input = choisir("Vous pouvez défausser au maximum 1 carte", new ArrayList<>(), boutons, true);

            if(!input.equalsIgnoreCase("")){
                for(int i=0; i<resultat.size(); i++){
                    if(resultat.get(i).getNom().equalsIgnoreCase(input)){
                        resultat.remove(i);
                        break;
                    }
                }
            }
        }

        return resultat;

    }

    public int nbWagonMemeCouleurMax(){
        ArrayList<CouleurWagon> dejasVus = new ArrayList<>();
        int max = 0;

        for(int i=0; i<this.cartesWagon.size(); i++){
            if(!dejasVus.contains(this.cartesWagon.get(i))){
                int cmpt = 0;
                for(int x=0; x<this.cartesWagon.size(); x++){
                    if(this.cartesWagon.get(x).equals(this.cartesWagon.get(i)) || this.cartesWagon.get(x).equals(CouleurWagon.LOCOMOTIVE)){
                        cmpt++;
                    }
                }

                if(cmpt > max){
                    max = cmpt;
                }

            }
        }

        return max;
    }

    public void jouerTourPoserGare(){

    }
}
