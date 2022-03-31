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

    public int getNbGares() {
        return nbGares;
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

        ArrayList<String> wagonVisibleString = new ArrayList<>();
        ArrayList<CouleurWagon> wagonVisible = new ArrayList<>();
        for(CouleurWagon cw : this.jeu.getCartesWagonVisibles()){
            wagonVisible.add(cw);
            wagonVisibleString.add(cw.toString().toUpperCase());
        }

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
        }else if(wagonVisibleString.contains(choix) || choix.equalsIgnoreCase(CouleurWagon.GRIS.toString())){
            //CHOISI: WAGON
            if(wagonVisibleString.contains(choix)){
                //1er carte wagon VISIBLE
                if(choix.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                    //1 seule carte à piocher
                    this.jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.add(CouleurWagon.LOCOMOTIVE);
                }else {
                    //2 cartes à piocher
                    CouleurWagon cwToRemove = null;
                    for(int i=0; i<wagonVisible.size(); i++){
                        if(wagonVisible.get(i).toString().equalsIgnoreCase(choix)){
                            cwToRemove = wagonVisible.get(i);
                            this.cartesWagon.add(wagonVisible.get(i));
                            wagonVisible.remove(i);
                            wagonVisibleString.remove(i);
                            break;
                        }
                    }
                    if(cwToRemove != null){
                        this.jeu.retirerCarteWagonVisible(cwToRemove);

                        this.secondTourDeSelectionCarteWagon(wagonVisibleString, wagonVisible);
                    }else {
                        this.jeu.log("ERROR: Cartes non visibles.");
                    }
                }
            }else {
                //1er Carte NON VISIBLE
                if(this.jeu.getPileCartesWagon().isEmpty()){
                    this.jeu.log("Impossible de piocher une carte wagon non visible.");
                    this.jouerTour();
                }else {
                    CouleurWagon selectCardNotVisible = this.jeu.piocherCarteWagon();
                    this.cartesWagon.add(selectCardNotVisible);

                    this.secondTourDeSelectionCarteWagon(wagonVisibleString, wagonVisible);
                }

            }

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
                            cartesPossibles.add(this.cartesWagon.get(i).toString().toUpperCase());
                        }
                        String choixCarte = this.choisir("Choisir la carte à défausser", new ArrayList<>(), cartesPossibles, false);

                        for(int i=0; i<this.cartesWagon.size(); i++){
                            if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixCarte)){
                                this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                this.cartesWagon.remove(i);
                                break;
                            }
                        }

                        /*ArrayList<String> finalChose = new ArrayList<>();
                        finalChose.add("Oui"); finalChose.add("Non, tous annulé");
                        String choixFinal = this.choisir("Êtes-vous sûre de vouloir prendre la gâre de " + villeChoisis.getNom() + ", avec la carte " + this.cartesWagonPosees.get(0).toString() + " ?", new ArrayList<>(), finalChose, false);*/

                        villeChoisis.setProprietaire(this);
                        this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(0));
                        this.cartesWagonPosees.remove(0);
                        this.nbGares--;
                        this.score += 4;

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
                            cartesPossibles.add(this.cartesWagon.get(i).toString().toUpperCase());
                            cartesSaves.add(this.cartesWagon.get(i).toString().toUpperCase());
                        }

                        String choixCarte1;
                        String choixCarte2;
                        CouleurWagon choixCWCarte1 = null;
                        CouleurWagon choixCWCarte2 = null;

                        if(this.nbWagonMemeCouleurMax() == 2 && this.cartesWagon.size() == 2){
                            choixCarte1 = this.cartesWagon.get(0).toString();
                            choixCarte2 = this.cartesWagon.get(1).toString();
                        }else {
                            do{
                                choixCarte1 = this.choisir("Choisir la 1er carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                if(choixCarte1.equalsIgnoreCase("")){
                                    this.jouerTour();
                                }

                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte1)){
                                        choixCWCarte1 = this.cartesWagon.get(j);
                                        break;
                                    }
                                }
                            }while(this.getNbWagonByCoul(choixCWCarte1) + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < 2);

                            for(int j=0; j<this.cartesWagon.size(); j++){
                                if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte1)){
                                    this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                    this.cartesWagon.remove(j);
                                    break;
                                }
                            }

                            for(int i=0; i<cartesPossibles.size(); i++){
                                if(cartesPossibles.get(i).equalsIgnoreCase(choixCWCarte1.toString())){
                                    cartesPossibles.remove(i);
                                    cartesSaves.remove(i);
                                    break;
                                }
                            }

                            boolean whileCheck2 = false;
                            do{
                                choixCarte2 = this.choisir("Choisir la 2nd carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                if(choixCarte2.equalsIgnoreCase("")){
                                    this.jouerTour();

                                    for(int w=0; w<this.cartesWagonPosees.size(); w++){
                                        this.cartesWagon.add(this.cartesWagonPosees.get(0));
                                        this.cartesWagonPosees.remove(0);
                                    }
                                }

                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte2)){
                                        choixCWCarte2 = this.cartesWagon.get(j);
                                        this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                        this.cartesWagon.remove(j);
                                        break;
                                    }
                                }

                                cartesPossibles.clear();
                                cartesPossibles.addAll(cartesSaves);

                                if(!choixCWCarte2.equals(choixCWCarte1) && !choixCWCarte2.equals(CouleurWagon.LOCOMOTIVE)){
                                    this.cartesWagon.add(this.cartesWagonPosees.get(this.cartesWagonPosees.size()-1));
                                    this.cartesWagonPosees.remove(this.cartesWagonPosees.size()-1);
                                }else {
                                    whileCheck2 = true;
                                }
                            }while(whileCheck2 == false);
                        }

                        villeChoisis.setProprietaire(this);
                        this.score += 4;
                        int nbDefausse = this.cartesWagonPosees.size();
                        for(int i=0; i<nbDefausse; i++){
                            this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(0));
                            this.cartesWagonPosees.remove(0);
                        }
                        this.cartesWagonPosees.clear();
                        this.nbGares--;

                    }else {
                        log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, vous n'avez pas assez de cartes wagons de même couleur");
                        this.jouerTour();
                    }
                }else if(this.nbGares == 1){
                    //3nd Gare => 3 wagon de mm couleur
                    if(this.nbWagonMemeCouleurMax() >= 3){
                        ArrayList<String> cartesPossibles = new ArrayList<>();
                        ArrayList<String> cartesSaves = new ArrayList<>();
                        for(int i=0; i<this.cartesWagon.size(); i++){
                            cartesPossibles.add(this.cartesWagon.get(i).toString().toUpperCase());
                            cartesSaves.add(this.cartesWagon.get(i).toString().toUpperCase());
                        }

                        String choixCarte1 = "";
                        String choixCarte2 = "";
                        String choixCarte3 = "";
                        CouleurWagon choixCWCarte1 = null;
                        CouleurWagon choixCWCarte2 = null;
                        CouleurWagon choixCWCarte3 = null;

                        if(this.nbWagonMemeCouleurMax() == 3 && this.cartesWagon.size() == 3){
                            choixCWCarte1 = this.cartesWagon.get(0);
                            choixCWCarte2 = this.cartesWagon.get(1);
                            choixCWCarte3 = this.cartesWagon.get(2);
                        }else {
                            do{
                                choixCarte1 = this.choisir("Choisir la 1er carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                if(choixCarte1.equalsIgnoreCase("")){
                                    this.jouerTour();
                                }

                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte1)){
                                        choixCWCarte1 = this.cartesWagon.get(j);
                                        break;
                                    }
                                }
                            }while(this.getNbWagonByCoul(choixCWCarte1) + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < 3);

                            for(int j=0; j<this.cartesWagon.size(); j++){
                                if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte1)){
                                    this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                    this.cartesWagon.remove(j);
                                    break;
                                }
                            }

                            for(int i=0; i<cartesPossibles.size(); i++){
                                if(cartesPossibles.get(i).equalsIgnoreCase(choixCWCarte1.toString())){
                                    cartesPossibles.remove(i);
                                    cartesSaves.remove(i);
                                    break;
                                }
                            }

                            boolean whileCheck2 = false;
                            do{
                                choixCarte2 = this.choisir("Choisir la 2nd carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                if(choixCarte2.equalsIgnoreCase("")){
                                    this.jouerTour();

                                    for(int w=0; w<this.cartesWagonPosees.size(); w++){
                                        this.cartesWagon.add(this.cartesWagonPosees.get(0));
                                        this.cartesWagonPosees.remove(0);
                                    }
                                }

                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte2)){
                                        choixCWCarte2 = this.cartesWagon.get(j);
                                        this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                        this.cartesWagon.remove(j);
                                        break;
                                    }
                                }

                                cartesPossibles.clear();
                                cartesPossibles.addAll(cartesSaves);

                                if(!choixCWCarte2.equals(choixCWCarte1) && !choixCWCarte2.equals(CouleurWagon.LOCOMOTIVE)){
                                    this.cartesWagon.add(this.cartesWagonPosees.get(this.cartesWagonPosees.size()-1));
                                    this.cartesWagonPosees.remove(this.cartesWagonPosees.size()-1);
                                }else {
                                    whileCheck2 = true;
                                }
                            }while(whileCheck2 == false);



                            for(int i=0; i<cartesPossibles.size(); i++){
                                if(cartesPossibles.get(i).equalsIgnoreCase(choixCWCarte2.toString())){
                                    cartesPossibles.remove(i);
                                    cartesSaves.remove(i);
                                    break;
                                }
                            }

                            boolean whileCheck3 = false;
                            do{
                                choixCarte3 = this.choisir("Choisir la 3eme carte à défausser", new ArrayList<>(), cartesPossibles, false);

                                if(choixCarte3.equalsIgnoreCase("")){
                                    this.jouerTour();

                                    for(int w=0; w<this.cartesWagonPosees.size(); w++){
                                        this.cartesWagon.add(this.cartesWagonPosees.get(0));
                                        this.cartesWagonPosees.remove(0);
                                    }
                                }

                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixCarte3)){
                                        choixCWCarte3 = this.cartesWagon.get(j);
                                        this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                        this.cartesWagon.remove(j);
                                        break;
                                    }
                                }

                                cartesPossibles.clear();
                                cartesPossibles.addAll(cartesSaves);

                                if(!choixCWCarte3.equals(choixCWCarte1) && !choixCWCarte3.equals(CouleurWagon.LOCOMOTIVE)){
                                    this.cartesWagon.add(this.cartesWagonPosees.get(this.cartesWagonPosees.size()-1));
                                    this.cartesWagonPosees.remove(this.cartesWagonPosees.size()-1);
                                }else {
                                    whileCheck3 = true;
                                }
                            }while(whileCheck3 == false);
                        }

                        villeChoisis.setProprietaire(this);
                        this.score += 4;
                        int nbDefausse = this.cartesWagonPosees.size();
                        for(int i=0; i<nbDefausse; i++){
                            this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(0));
                            this.cartesWagonPosees.remove(0);
                        }
                        this.cartesWagonPosees.clear();
                        this.nbGares--;

                    }else {
                        log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, vous n'avez pas assez de cartes wagons de même couleur");
                        this.jouerTour();
                    }

                }else {
                    log(this.nom + " il est <strong>impossible</strong> pour vous de contruire une gare, nombre de gares incompris.");
                    this.jouerTour();
                }
            }
        }else if(routeSelect.contains(choix)){
            //CHOISI: ROUTE
            Route routeChoisi = null;

            //Je get la bonne route dans routeChosi
            for(int i=0; i<this.jeu.getRoutes().size(); i++){
                if(this.jeu.getRoutes().get(i).getNom().equalsIgnoreCase(choix)){
                    routeChoisi = this.jeu.getRoutes().get(i);
                    break;
                }
            }

            if(this.nbWagons < routeChoisi.getLongueur()){
                this.jeu.log("Impossible de construire une route, vous n'avez pas assez de <strong>Nombre de Wagon</strong>.");
                this.jouerTour();
            }else {
                if(routeChoisi.getProprietaire() != null){
                    this.jouerTour();
                }
                if(routeChoisi.estFerry() == true){
                    //FERRY
                    routeChoisi = (Ferry) routeChoisi;

                    if(this.nbWagonMemeCouleurMax() + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < ((Ferry) routeChoisi).getLongueur() && this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < ((Ferry) routeChoisi).getNbLocomotives()){
                        this.jouerTour();
                    }else {
                        int nbLocoChosePlayer = 0;
                        do{
                            for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                this.cartesWagon.add(this.cartesWagonPosees.get(i));
                            }
                            this.cartesWagonPosees.clear();
                            nbLocoChosePlayer = 0;
                            ArrayList<String> buttonWagonVisibleString = new ArrayList<>();
                            String colorToFillAll = "";
                            do{
                                buttonWagonVisibleString.clear();
                                String choixPlayer = "";
                                boolean whileCheck = true;
                                do {
                                    buttonWagonVisibleString.clear();
                                    for(int i=0; i<this.cartesWagon.size(); i++){
                                        buttonWagonVisibleString.add(this.cartesWagon.get(i).toString().toUpperCase());
                                    }
                                    choixPlayer = this.choisir(
                                            "Choissisez vos cartes.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonWagonVisibleString,
                                            true);//Le joueur ne peut pas passer.

                                    if (choixPlayer.equalsIgnoreCase("")){
                                        for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                            this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                        }
                                        this.cartesWagonPosees.clear();
                                        this.jouerTour();
                                    }else {
                                        if(!choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                            if(colorToFillAll.equalsIgnoreCase("")){
                                                for(int i=0; i<this.cartesWagon.size(); i++){
                                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                                        colorToFillAll = this.cartesWagon.get(i).toString().toUpperCase();
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if(choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString().toUpperCase()) || choixPlayer.equalsIgnoreCase(colorToFillAll)){
                                            whileCheck = false;
                                        }
                                    }
                                }while(whileCheck == true);

                                for(int i=0; i<this.cartesWagon.size(); i++){
                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                        this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                        this.cartesWagon.remove(i);
                                        break;
                                    }
                                }


                            }while(this.cartesWagonPosees.size() < routeChoisi.getLongueur());
                            for(CouleurWagon cw : this.cartesWagonPosees){
                                if(cw.equals(CouleurWagon.LOCOMOTIVE)){
                                    nbLocoChosePlayer++;
                                }
                            }
                        }while(nbLocoChosePlayer < ((Ferry) routeChoisi).getNbLocomotives());

                        nbLocoChosePlayer = 0;
                        for(CouleurWagon cw : this.cartesWagonPosees){
                            if(cw.equals(CouleurWagon.LOCOMOTIVE)){
                                nbLocoChosePlayer++;
                            }
                        }
                        if(nbLocoChosePlayer<((Ferry) routeChoisi).getNbLocomotives()){
                            for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                this.cartesWagon.add(this.cartesWagonPosees.get(j));
                            }
                            this.cartesWagonPosees.clear();
                            this.jouerTour();
                        }

                        routeChoisi.setProprietaire(this);

                        for(int j=0; j<this.cartesWagonPosees.size(); j++){
                            this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(j));
                        }
                        this.cartesWagonPosees.clear();
                        this.addScoreEnFonctionDeRoute(routeChoisi);
                        this.nbWagons-=routeChoisi.getLongueur();
                    }
                }else if(routeChoisi.estTunnel() == true){
                    //TUNEL
                    routeChoisi = (Tunnel) routeChoisi;

                    if(routeChoisi.getCouleur().equals(CouleurWagon.GRIS)){
                        if(this.nbWagonMemeCouleurMax() + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < routeChoisi.getLongueur()){
                            this.jouerTour();
                        }else {
                            ArrayList<String> buttonStringCartWagonPlayer = new ArrayList<>();
                            String colorToFillAll = "";
                            do{
                                buttonStringCartWagonPlayer.clear();
                                String choixPlayer = "";
                                boolean whileCheck = true;
                                do {
                                    buttonStringCartWagonPlayer.clear();
                                    for(int i=0; i<this.cartesWagon.size(); i++){
                                        buttonStringCartWagonPlayer.add(this.cartesWagon.get(i).toString().toUpperCase());
                                    }
                                    choixPlayer = this.choisir(
                                            "Choissisez vos cartes.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonStringCartWagonPlayer,
                                            true);//Le joueur ne peut pas passer.

                                    if (choixPlayer.equalsIgnoreCase("")){
                                        for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                            this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                        }
                                        this.cartesWagonPosees.clear();
                                        this.jouerTour();
                                    }else {
                                        if(!choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                            if(colorToFillAll.equalsIgnoreCase("")){
                                                for(int i=0; i<this.cartesWagon.size(); i++){
                                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                                        colorToFillAll = this.cartesWagon.get(i).toString().toUpperCase();
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if(choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString().toUpperCase()) || choixPlayer.equalsIgnoreCase(colorToFillAll)){
                                            whileCheck = false;
                                        }
                                    }
                                }while(whileCheck == true);

                                for(int i=0; i<this.cartesWagon.size(); i++){
                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                        this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                        this.cartesWagon.remove(i);
                                        break;
                                    }
                                }

                            }while(this.cartesWagonPosees.size() < routeChoisi.getLongueur());

                            //PARTIE SPECIAL TUNNEL
                            //Calcul du nombre de carte à ajouter pour le joueur
                            int nbCardToAdd = 0;
                            for(int i=0; i<3; i++){
                                CouleurWagon c = this.jeu.piocherCarteWagon();
                                if(c.equals(routeChoisi.getCouleur())){
                                    nbCardToAdd++;
                                }
                                this.jeu.defausserCarteWagon(c);
                            }

                            while(this.cartesWagonPosees.size() < (routeChoisi.getLongueur() + nbCardToAdd)) {
                                String choixPlayer = "";
                                do{
                                    buttonStringCartWagonPlayer.clear();
                                    for(int i=0; i<this.cartesWagon.size(); i++){
                                        buttonStringCartWagonPlayer.add(this.cartesWagon.get(i).toString().toUpperCase());
                                    }
                                    choixPlayer = this.choisir(
                                            "Ajouter encore des cartes.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonStringCartWagonPlayer,
                                            true);//Le joueur ne peut pas passer.

                                    if (choixPlayer.equalsIgnoreCase("")){
                                        for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                            this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                        }
                                        this.cartesWagonPosees.clear();
                                        this.jouerTour();
                                    }
                                }while(!choixPlayer.equalsIgnoreCase(colorToFillAll) && !choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()));

                                for(int i=0; i<this.cartesWagon.size(); i++){
                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                        this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                        this.cartesWagon.remove(i);
                                        break;
                                    }
                                }
                            }

                            routeChoisi.setProprietaire(this);

                            for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(j));
                            }
                            this.cartesWagonPosees.clear();
                            this.addScoreEnFonctionDeRoute(routeChoisi);
                            this.nbWagons-=routeChoisi.getLongueur();
                        }
                    }else {
                        if(this.getNbWagonByCoul(routeChoisi.getCouleur()) + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) < routeChoisi.getLongueur()){
                            this.jouerTour();
                        }else {
                            ArrayList<String> buttonStringCartWagonPlayer = new ArrayList<>();
                            do {
                                String choixPlayer = "";
                                do{
                                    buttonStringCartWagonPlayer.clear();
                                    for(int i=0; i<this.cartesWagon.size(); i++){
                                        buttonStringCartWagonPlayer.add(this.cartesWagon.get(i).toString().toUpperCase());
                                    }
                                    choixPlayer = this.choisir(
                                            "Choissisez vos cartes.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonStringCartWagonPlayer,
                                            true);//Le joueur ne peut pas passer.

                                    if (choixPlayer.equalsIgnoreCase("")){
                                        for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                            this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                        }
                                        this.cartesWagonPosees.clear();
                                        this.jouerTour();
                                    }
                                }while(!choixPlayer.equalsIgnoreCase(routeChoisi.getCouleur().toString()) && !choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()));

                                for(int i=0; i<this.cartesWagon.size(); i++){
                                    if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                        this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                        this.cartesWagon.remove(i);
                                        break;
                                    }
                                }
                            }while(this.cartesWagonPosees.size() < routeChoisi.getLongueur());

                            //PARTIE SPECIAL TUNNEL
                            //Calcul du nombre de carte à ajouter pour le joueur
                            int nbCardToAdd = 0;
                            for(int i=0; i<3; i++){
                                CouleurWagon c = this.jeu.piocherCarteWagon();
                                if(c.equals(routeChoisi.getCouleur())){
                                    nbCardToAdd++;
                                }
                                this.jeu.defausserCarteWagon(c);
                            }

                            if((this.getNbWagonByCoul(routeChoisi.getCouleur()) + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE)) < nbCardToAdd){
                                for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                    this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                }
                                this.cartesWagonPosees.clear();
                            }else {
                                boolean whileCheckWhile = true;
                                boolean pass = false;
                                while(this.cartesWagonPosees.size() < (routeChoisi.getLongueur() + nbCardToAdd) && pass == false) {
                                    String choixPlayer = "";
                                    do{
                                        buttonStringCartWagonPlayer.clear();
                                        for(int i=0; i<this.cartesWagon.size(); i++){
                                            buttonStringCartWagonPlayer.add(this.cartesWagon.get(i).toString().toUpperCase());
                                        }
                                        choixPlayer = this.choisir(
                                                "Ajouter encore des cartes.", // instruction
                                                new ArrayList<>(), // choix (hors boutons, ici aucun)
                                                buttonStringCartWagonPlayer,
                                                true);//Le joueur ne peut pas passer.

                                        if (choixPlayer.equalsIgnoreCase("")){
                                            for(int i=0; i<this.cartesWagonPosees.size(); i++){
                                                this.cartesWagon.add(this.cartesWagonPosees.get(i));
                                            }
                                            this.cartesWagonPosees.clear();
                                            whileCheckWhile = false;
                                            pass = true;
                                            //this.jouerTour();
                                        }

                                        if(whileCheckWhile == true && pass == false){
                                            if(choixPlayer.equalsIgnoreCase(routeChoisi.getCouleur().toString()) || choixPlayer.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                                                whileCheckWhile = false;
                                            }
                                        }
                                    }while(whileCheckWhile == true);

                                    if(pass == false) {
                                        for(int i=0; i<this.cartesWagon.size(); i++){
                                            if(this.cartesWagon.get(i).toString().equalsIgnoreCase(choixPlayer)){
                                                this.cartesWagonPosees.add(this.cartesWagon.get(i));
                                                this.cartesWagon.remove(i);
                                                break;
                                            }
                                        }
                                    }
                                }

                                if(pass == false){
                                    routeChoisi.setProprietaire(this);

                                    for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                        this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(j));
                                    }
                                    this.cartesWagonPosees.clear();
                                    this.addScoreEnFonctionDeRoute(routeChoisi);
                                    this.nbWagons-=routeChoisi.getLongueur();
                                }
                            }
                        }
                    }
                }else {
                    if(routeChoisi.getCouleur().equals(CouleurWagon.GRIS)){
                        //N'importe quel couleur !
                        if(this.cartesWagon.size() >= routeChoisi.getLongueur()){

                            //ROUTE NORMALE
                            ArrayList<String> buttonStringWagonListWagonPlayer = new ArrayList<>();
                            for(int j=0; j<this.cartesWagon.size(); j++){
                                buttonStringWagonListWagonPlayer.add(this.cartesWagon.get(j).toString().toUpperCase());
                            }

                            CouleurWagon colorWagonChoseToSetRoad = null;
                            int nbCardToSetRoad = 0;
                            do{
                                String choisirLaPremierCarteWagonAPoser = this.choisir(
                                        "Choissisez la 1er carte wagon.", // instruction
                                        new ArrayList<>(), // choix (hors boutons, ici aucun)
                                        buttonStringWagonListWagonPlayer,
                                        true);//Le joueur ne peut pas passer.

                                if(choisirLaPremierCarteWagonAPoser.equalsIgnoreCase("")){
                                    this.jouerTour();
                                }else{
                                    for(CouleurWagon cw : this.cartesWagon){
                                        if(cw.toString().equalsIgnoreCase(choisirLaPremierCarteWagonAPoser)){
                                            colorWagonChoseToSetRoad = cw;
                                            break;
                                        }
                                    }
                                }
                                nbCardToSetRoad = this.getNbWagonByCoul(colorWagonChoseToSetRoad) + this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE);
                            }while(nbCardToSetRoad < routeChoisi.getLongueur());

                            //On supprime la carte de ces carte de la main du joueur & on la pose.
                            for(int j=0; j<this.cartesWagon.size(); j++){
                                if(this.cartesWagon.get(j).equals(colorWagonChoseToSetRoad)){
                                    this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                    this.cartesWagon.remove(j);
                                    break;
                                }
                            }

                            int i = 1;
                            while (i<routeChoisi.getLongueur()){
                                buttonStringWagonListWagonPlayer.clear();
                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    buttonStringWagonListWagonPlayer.add(this.cartesWagon.get(j).toString().toUpperCase());
                                }
                                String choixWagonToDefausseToCreateRoad = "";
                                boolean whileCheck = false;
                                do{
                                    choixWagonToDefausseToCreateRoad = this.choisir(
                                            "Choissisez la " + (i+1) + "eme carte wagon.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonStringWagonListWagonPlayer,
                                            true);//Le joueur ne peut pas passer.

                                    if(choixWagonToDefausseToCreateRoad.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) || choixWagonToDefausseToCreateRoad.equalsIgnoreCase(colorWagonChoseToSetRoad.toString())){
                                        whileCheck = true;
                                    }
                                }while(whileCheck == false);

                                if(choixWagonToDefausseToCreateRoad.equalsIgnoreCase("")){
                                    for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                        this.cartesWagon.add(this.cartesWagonPosees.get(j));
                                    }
                                    this.cartesWagonPosees.clear();
                                    this.jouerTour();
                                }else {
                                    //On supprime la carte de ces carte de la main du joueur & on la pose.
                                    for(int j=0; j<this.cartesWagon.size(); j++){
                                        if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixWagonToDefausseToCreateRoad)){
                                            this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                            this.cartesWagon.remove(j);
                                            break;
                                        }
                                    }
                                }

                                i++;
                            }
                            routeChoisi.setProprietaire(this);
                            for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(j));
                            }

                            this.cartesWagonPosees.clear();
                            this.addScoreEnFonctionDeRoute(routeChoisi);
                            this.nbWagons-=routeChoisi.getLongueur();
                        }else {
                            this.jeu.log("ERREUR, nombre de wagon invalide !");
                            this.jouerTour();
                        }
                    }else {
                        //Route de couleur
                        int nbLocoAndColorCardWagon = this.getNbWagonByCoul(CouleurWagon.LOCOMOTIVE) + this.getNbWagonByCoul(routeChoisi.getCouleur());
                        if(nbLocoAndColorCardWagon >= routeChoisi.getLongueur()){
                            //ROUTE NORMALE
                            int i = 0;
                            while (i<routeChoisi.getLongueur()){
                                ArrayList<String> buttonStringWagonListWagonPlayer = new ArrayList<>();
                                for(int j=0; j<this.cartesWagon.size(); j++){
                                    buttonStringWagonListWagonPlayer.add(this.cartesWagon.get(j).toString().toUpperCase());
                                }
                                String choixWagonToDefausseToCreateRoad = "";

                                do {
                                    choixWagonToDefausseToCreateRoad = this.choisir(
                                            "Choissisez la " + (i+1) + "eme carte wagon.", // instruction
                                            new ArrayList<>(), // choix (hors boutons, ici aucun)
                                            buttonStringWagonListWagonPlayer,
                                            true);//Le joueur ne peut pas passer.
                                }while(!choixWagonToDefausseToCreateRoad.equalsIgnoreCase(routeChoisi.getCouleur().toString()) && !choixWagonToDefausseToCreateRoad.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) && !choixWagonToDefausseToCreateRoad.equalsIgnoreCase(""));

                                if(choixWagonToDefausseToCreateRoad.equalsIgnoreCase("")){
                                    for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                        this.cartesWagon.add(this.cartesWagonPosees.get(j));
                                    }
                                    this.cartesWagonPosees.clear();
                                    this.jouerTour();
                                }else {
                                    //On supprime la carte de ces carte de la main du joueur & on la pose.
                                    for(int j=0; j<this.cartesWagon.size(); j++){
                                        if(this.cartesWagon.get(j).toString().equalsIgnoreCase(choixWagonToDefausseToCreateRoad)){
                                            this.cartesWagonPosees.add(this.cartesWagon.get(j));
                                            this.cartesWagon.remove(j);
                                            break;
                                        }
                                    }
                                }

                                i++;
                            }
                            routeChoisi.setProprietaire(this);
                            for(int j=0; j<this.cartesWagonPosees.size(); j++){
                                this.jeu.defausserCarteWagon(this.cartesWagonPosees.get(j));
                            }

                            this.cartesWagonPosees.clear();
                            this.addScoreEnFonctionDeRoute(routeChoisi);
                            this.nbWagons-=routeChoisi.getLongueur();
                        }else {
                            this.jeu.log("ERREUR, nombre de wagon invalide !");
                            this.jouerTour();
                        }
                    }
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

    public int getNbWagonByCoul(CouleurWagon coul){
        int res = 0;

        for(int i=0; i<this.cartesWagon.size(); i++){
            if(this.cartesWagon.get(i).toString().equalsIgnoreCase(coul.toString())){
                res++;
            }
        }

        return res;
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

    public void secondTourDeSelectionCarteWagon(ArrayList<String> wagonVisibleString, ArrayList<CouleurWagon> wagonVisible){
        //2EME CHOIX
        ArrayList<String> deuxiemeChoixAL = new ArrayList<>();
        for(int i=0; i<wagonVisibleString.size(); i++){
            wagonVisibleString.remove(0);
        }
        for(int i=0; i<wagonVisible.size(); i++){
            wagonVisible.remove(0);
        }
        for(CouleurWagon cw : this.jeu.getCartesWagonVisibles()){
            wagonVisible.add(cw);
            wagonVisibleString.add(cw.toString().toUpperCase());
        }
        deuxiemeChoixAL.addAll(wagonVisibleString);
        deuxiemeChoixAL.add(CouleurWagon.GRIS.toString().toUpperCase());
        String deuxiemeChoixCarteWagon = this.choisir(
                "Choissisez votre autre carte Wagon.", // instruction
                deuxiemeChoixAL, // choix (hors boutons, ici aucun)
                new ArrayList<>(),
                true);

        //Si le joueur n'as pas choisis une carte face cachée
        if(!deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.GRIS.toString()) && !deuxiemeChoixCarteWagon.equalsIgnoreCase("")){
            //Si la carte selectionner est une LOCOMOTIVE & elle n'est pas contenue dans les cartes visibles et grises && elle ce n'est pas passer alors:
            boolean whileCheck = true;
            if((deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) || !wagonVisibleString.contains(deuxiemeChoixCarteWagon)) && !deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.GRIS.toString())){
                do{
                    deuxiemeChoixCarteWagon = this.choisir(
                            "Choissisez l'autre carte Wagon.", // instruction
                            deuxiemeChoixAL, // choix (hors boutons, ici aucun)
                            new ArrayList<>(),
                            true);
                }while((deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString()) || !wagonVisibleString.contains(deuxiemeChoixCarteWagon)) && !deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.GRIS.toString()));
            }

        }


        if(wagonVisibleString.contains(deuxiemeChoixCarteWagon)){
            //2eme carte wagon VISIBLE
            for(int i=0; i<wagonVisible.size(); i++){
                if(wagonVisible.get(i).toString().equalsIgnoreCase(deuxiemeChoixCarteWagon)){
                    this.cartesWagon.add(wagonVisible.get(i));
                    this.jeu.retirerCarteWagonVisible(wagonVisible.get(i));
                    break;
                }
            }
        }else if(deuxiemeChoixCarteWagon.equalsIgnoreCase(CouleurWagon.GRIS.toString())){
            //2eme carte wagon NON VISIBLE

            if(this.jeu.getPileCartesWagon().isEmpty()){
                this.jeu.log("Impossible, de piocher une carte wagon non visible, car aucune carte dans la pile.");
                this.jouerTour();
            }else {
                CouleurWagon selectCardNotVisible = this.jeu.piocherCarteWagon();
                while(selectCardNotVisible.toString().equalsIgnoreCase(CouleurWagon.LOCOMOTIVE.toString())){
                    //this.jeu.defausserCarteWagon(selectCardNotVisible);
                    int nombreAuHasardPourRemettreDansLeJeu = (int) (Math.random() * (this.jeu.getPileCartesWagon().size() - 1)) + 1;
                    this.jeu.getPileCartesWagon().add(nombreAuHasardPourRemettreDansLeJeu, selectCardNotVisible);
                    this.jeu.getPileCartesWagon().remove(0);
                    selectCardNotVisible = this.jeu.piocherCarteWagon();
                }

                this.cartesWagon.add(selectCardNotVisible);
            }


        }
    }
}
