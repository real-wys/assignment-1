import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.Calendar;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

public class Test {
    public static void getCurrentDate() {
        get("http://localhost:4567/today/date").then().statusCode(200).body(Matchers.equalTo(Main.getDate()));
    }
    public static void getCurrentDay() {
        get("http://localhost:4567/today/day").then().statusCode(200).body(Matchers.equalTo(Integer.toString(Main.getDay())));
    }
    public static void getCurrentMonth() {
        get("http://localhost:4567/today/month").then().statusCode(200).body(Matchers.equalTo(Integer.toString(Main.getMonth())));
    }
    public static void getCurrentYear() {
        get("http://localhost:4567/today/year").then().statusCode(200).body(Matchers.equalTo(Integer.toString(Main.getYear())));
    }
    public static String addEvent() {
        Main.Event toAdd = new Main.Event("12-02-2022","celebration");
        Gson gson = new Gson();
        String toAddJson = gson.toJson(toAdd);
        RequestSpecification req = RestAssured.given();
        req.body(toAddJson);
        Response response = req.post("http://localhost:4567/events");
        Assert.assertEquals(response.statusCode(),200);
        Assert.assertEquals(response.header("res"),"success");
        String id = response.header("id");
        return id;
    }
    public static void updateEvent(String id) {
        Main.Event toAdd = new Main.Event("12-03-2022","celebration");
        Gson gson = new Gson();
        String toAddJson = gson.toJson(toAdd);
        RequestSpecification req = RestAssured.given();
        req.body(toAddJson);
        Response response = req.put("http://localhost:4567/events?id="+id);
        Assert.assertEquals(response.statusCode(),200);
        Assert.assertEquals(response.header("res"),"success");
    }
    public static boolean checkInEventList(String date, String id) {
        RequestSpecification req = RestAssured.given();
        Response response = req.get("http://localhost:4567/events?date="+date);
        Assert.assertEquals(response.statusCode(),200);
        Assert.assertEquals(response.header("res"),"success");
        String events = response.header("events");
        Gson gson = new Gson();
        Main.Event[] evs = gson.fromJson(events, Main.Event[].class);
        for (Main.Event e : evs) {
            if (e.id.equals(id)) return true;
        }
        return false;
    }
    public static void deleteEvent(String id) {
        RequestSpecification req = RestAssured.given();
        Response response = req.delete("http://localhost:4567/events?id="+id);
        Assert.assertEquals(response.statusCode(),200);
        Assert.assertEquals(response.header("res"),"success");
    }
    public static void main(String[] args) {
        getCurrentDate();
        getCurrentDay();
        getCurrentMonth();
        getCurrentYear();
        String id = addEvent();
        Assert.assertEquals(checkInEventList("12-02-2022", id), true);
        updateEvent(id);
        Assert.assertEquals(checkInEventList("12-02-2022", id), false);
        Assert.assertEquals(checkInEventList("12-03-2022", id), true);
        deleteEvent(id);
        Assert.assertEquals(checkInEventList("12-03-2022", id), false);

    }
}
