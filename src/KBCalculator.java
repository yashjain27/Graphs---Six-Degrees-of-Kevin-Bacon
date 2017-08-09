import big.data.*;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * This is the driver class.
 *
 * @author Yash Jain
 *         SBU ID: 109885836
 *         email: yash.jain@stonybrook.edu
 *         HW 7 CSE 214
 *         Section 10 Daniel Scanteianu
 *         Grading TA: Anand Aiyer
 */
public class KBCalculator {

    public static void main(String[] args){
        //Scanners and variables
        Scanner input = new Scanner(System.in);
        String title = "", choice = "";

        //ActorGraph object
        ActorGraph graph = new ActorGraph();

        while(true){
            System.out.println("I) Import a Movie");
            System.out.println("A) Print all actors");
            System.out.println("M) Print all movies");
            System.out.println("P) Print the shortest path between two actors");
            System.out.println("B) Print the BFS from a given actor");
            System.out.println("L) Lookup Actor by Name");
            System.out.println("Q) Quit");
            choice = input.nextLine(); // Input choice

            switch (choice.toUpperCase()){
                case "I":
                    System.out.print("Enter a movie title: ");
                    //generate the web-address for the movie
                    String prefix= "http://www.omdbapi.com/?t=";
                    title=input.nextLine();
                    String postfix="&y=&plot=short&r=xml";
                    if(title.length()>0){
                        DataSource ds = DataSource.connectXML(prefix+title.replace(' ','+')+postfix);
                        ds.load();
                        try {
                            System.out.println(ds.fetchString("movie/title") + "(" +
                                    ds.fetchInt("movie/year") + ") Starring: "
                                    + ds.fetchString("movie/actors") + "\n");
                            String[] actors = ds.fetchString("movie/actors").split(", ");

                            //Create Movie object
                            Movie movie = new Movie(ds.fetchString("movie/title"));
                            movie.setYear(ds.fetchInt("movie/year"));

                            for(int i = 0; i < actors.length; i++){
                                Actor actor = new Actor();
                                actor.setName(actors[i]);
                                actor.setMovies(movie);
                                movie.setActors(actor);
                                try {
                                    graph.addActor(actors[i], actor);
                                } catch (ActorAlreadyExistsException e) {
                                    graph.getActor(actors[i]).setMovies(movie);
                                }
                            }
                            //Set up each actor's friends in the current movie
                            for(Actor a : movie.getActors())
                                for(Actor b : movie.getActors())
                                    if(!a.equals(b))
                                        graph.getActor(a.getName()).setFriends(graph.getActor(b.getName()));

                            //Set up the friends of actors that already existed and didn't get modified
                            for(String compareMovie : graph.getMoviesByTitle().keySet())
                                for(Actor actor1 : graph.getMoviesByTitle().get(compareMovie).getActors())
                                   for(Actor a : movie.getActors())
                                       if(actor1.getName().equals(a.getName()))
                                           for(Actor actor : movie.getActors())
                                               if(!actor1.getName().equals(actor.getName()))
                                                   actor1.setFriends(actor);

                            graph.addMovie(ds.fetchString("movie/title"), movie);
                        } catch (Exception e) {
                            System.out.println("Movie not found!\n");
                        }
                    }

                    break;
                case "A":
                    System.out.println(graph.printActors());
                    break;
                case "M":
                    System.out.println(graph.printMovies());
                    break;
                case "P":
                    //Clear the previous paths of actors
                    for(String act : graph.getActorsByName().keySet()){
                        graph.getActorsByName().get(act).getPath().clear();
                    }

                    System.out.println("Please enter the first name: ");
                    try {
                        ActorGraph.getBfs(input.nextLine());
                        System.out.println("Please enter the second name: ");
                        System.out.println(graph.getActor(input.nextLine()).printPath());
                    } catch (ActorDoesNotExistException e) {
                        System.out.println("Actor doesn't exist");
                    }

                    break;
                case "B":
                    System.out.print("Enter a starting actor: ");
                    LinkedList<String> actorBfs = null;
                    try {
                        actorBfs = ActorGraph.getBfs(input.nextLine());
                        System.out.println("Start bfs: ");
                        for(String s : actorBfs)
                            System.out.print(s + ", ");
                        System.out.println();
                    } catch (ActorDoesNotExistException e) {
                        System.out.println("Actor does't exist!");
                    }
                    break;
                case "L":
                    System.out.print("Enter a name: ");
                    try {
                        System.out.println(graph.getActor(input.nextLine()).toString());
                    } catch (ActorDoesNotExistException e) {
                        System.out.println("Actor not found!");
                    }
                    break;
                case "Q":
                    System.exit(1);
                default:
                    System.out.println("Invalid input. Please try again!");
            }
        }

    }
}
