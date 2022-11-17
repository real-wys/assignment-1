import java.nio.charset.Charset;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static spark.Spark.*;
public class Main {
    static Calendar c = Calendar.getInstance();
    static int getDay() {
        return c.get(Calendar.DAY_OF_MONTH);
    }
    static int getMonth() {
        return c.get(Calendar.MONTH);
    }
    static int getYear() {
        return c.get(Calendar.YEAR);
    }
    static String getDate() {
        return getMonth()+"-"+getDay()+"-"+getYear();
    }

    //key：Date(MM-DD-YYYY)
    static HashMap<String, HashSet<String>> map = new HashMap<>();
    static HashMap<String, Event> idMap = new HashMap<>();
    public static class Event {
        String date;
        String name;
        String id;
        public Event(String date, String name) {
            this.name = name;
            this.date = date;
        }
        public String toString() {
            return name + " " + date;
        }
        @Override
        public int hashCode() {
            return name.hashCode() * date.hashCode();
        }
    }
    /*Generate the id of a created event, once generated the id will not change*/
    private static String getRandomId() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i <32;i++) {
            int number = random.nextInt(62);
            stringBuffer.append(str.charAt(number));
        }
        return stringBuffer.toString();
    }
    public static void main(String[] args) {
        get("/today/date", (req, res) -> getDate());
        get("/today/day", (req, res) -> getDay());
        get("/today/month", (req, res) -> getMonth());
        get("/today/year", (req, res) -> getYear());
        /*Function: adding an events, return the event id generated*/
        post("/events", (req, res)-> {
            Gson gson = new Gson();
            Event toAdd = gson.fromJson(req.body(), Event.class);
            System.out.println(toAdd);
            try {
                HashSet<String> list = map.get(toAdd.date);
                if (list == null) {
                    list = new HashSet<>();
                }
                String id = getRandomId();
                toAdd.id = id;
                list.add(id);
                idMap.put(id, toAdd);
                map.put(toAdd.date, list);
                res.header("res","success");
                res.header("id",id);
            } catch (Exception e) {
                System.out.println(e);
            }
            return "[Success]Event added with id:" + toAdd.hashCode();
        });
        /*Function: updating an events given the event id*/
        put("/events", (req, res)-> {
            String id = req.queryParams("id");
            System.out.println("update id: "+ id);
            if (id == null || !idMap.containsKey(id)) {
                res.header("res","fail");
                return "[Fail]Event-" + id + "does not exist";
            }
            Gson gson = new Gson();
            Event toEdit = gson.fromJson(req.body(), Event.class);
            toEdit.id = id;
            Event origin = idMap.get(id);
            //if the date of the events has been changed
            if (!origin.date.equals(toEdit.date)) {
                map.get(origin.date).remove(id);
                HashSet<String> set = map.getOrDefault(toEdit.date, new HashSet<>());
                set.add(id);
                map.put(toEdit.date, set);
            }
            idMap.put(id, toEdit);
            res.header("res","success");
            return "[Success]Event-" + id + "has been updated";
        });
        /*Function: Delete an Event given the event id*/
        delete("/events", (req, res) -> {
            String id = req.queryParams("id");
            if (id == null || !idMap.containsKey(id)) {
                res.header("res","fail");
                return "[Fail]Event Id:" + id + "does not exist";
            }
            Event event  = idMap.get(id);
            HashSet<String> events = map.get(event.date);
            events.remove(id);
            map.put(event.date, events);
            res.header("res","success");
            return "[Success]Event-"+id+"has been deleted";
        });
        /*Function: Get an Event list given a particular date*/
        get("/events", (req, res)->{
            String date = req.queryParams("date");
            if (date == null) {
                res.header("res","fail");
                return "[Fail] date not found";
            }
            HashSet<String> events = map.get(date);
            if (events == null || events.size() == 0) {
                res.header("res","success");
                res.header("events","[]");
                return "[Success] No events in the given date:" + date;
            }
            List<Event> list = new ArrayList<>();
            for (String s : events) {
                list.add(idMap.get(s));
            }
            Gson gson = new Gson();
            String eventsToJson = gson.toJson(list);
            System.out.println("Events: "+eventsToJson);
            res.header("events",eventsToJson);
            res.header("res","success");
            return "[Success} events:" + eventsToJson;
        });
    }
}
