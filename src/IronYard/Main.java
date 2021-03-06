package IronYard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    static ArrayList<Message> messages  = new ArrayList<>();

    public static void main(String[] args) {

	    addTestUsers();
        addTestMessages();

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    String replyId = request.queryParams("replyId");
                    int replyIdNum = -1;
                    if(replyId != null){
                        replyIdNum = Integer.parseInt(replyId);
                    }
                    HashMap m = new HashMap();
                    ArrayList<Message> threads = new ArrayList<>();
                    for (Message message : messages){
                        if (message.replyId == replyIdNum){
                            threads.add(message);
                        }
                    }
                    Session session = request.session();
                    String userName = session.attribute("userName");

                    m.put("messages", threads);
                    m.put("userName", userName);
                    return new ModelAndView(m, "home.html");
                }),
        new MustacheTemplateEngine()
        );
    Spark.get(
            "/json",
            ((request, response) -> {
                String replyId = request.queryParams("replyId");
                int replyIdNum = -1;
                if(replyId != null){
                    replyIdNum = Integer.parseInt(replyId);
                }

                ArrayList<Message> threads = new ArrayList<>();
                for (Message message : messages){
                    if (message.replyId == replyIdNum){
                        threads.add(message);
                    }
                }

                JsonSerializer serializer = new JsonSerializer();
                String json = serializer.include("*").serialize(threads);
                return json;
            })
    );
    Spark.post(
            "/login",
            ((request, response) -> {
                String userName = request.queryParams("loginName");
                if (userName == null){
                    throw new Exception("login name not found");
                }

                User user = users.get(userName);
                if (user == null){
                    user = new User(userName);
                    users.put(userName, user);
                }

                Session session = request.session();
                session.attribute("userName", userName);

                response.redirect("/");
                return "";
            })

        );
    Spark.post(
            "/logout",
            ((request, response) -> {
                Session session = request.session();
                session.invalidate();
                response.redirect("/");
                return"";
            })
    );
    }
    static void addTestUsers(){
        users.put("Alice", new User("Alice"));
        users.put("Bob", new User("Bob"));
        users.put("Charlie", new User("Charlie"));
    }
    static void addTestMessages(){
        messages.add(new Message(0,-1, "Alice","Hello World!"));
        messages.add(new Message(1,-1, "Bob","HI love it!"));
        messages.add(new Message(2,0, "Charlie","Mee too!"));
        messages.add(new Message(3,2, "Alice","Thanks!"));
    }


}
