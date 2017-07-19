package com.qait.learning.com.rest;



import org.testng.annotations.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import net.minidev.json.JSONObject;
import org.testng.annotations.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class SnlV_3 {
	
	String accessToken, boardID,pathParam,playerID,playerPath,playerName;	
	@BeforeTest
	public void generateToken(){
		
		String token=RestAssured.given().parameters("Username", "su", "Password","root_pass","grant_type","client_credentials","client_id",
				"1f56f98a75a0a0dc966623e7d91ab1db7a96c15d3677d40ab07908097c89d603","client-Secret","f48ace1ba734e4bad2c43b13ab6f836cadcc592420802b91b1af5ffd16491455")
				.auth().preemptive().basic("1f56f98a75a0a0dc966623e7d91ab1db7a96c15d3677d40ab07908097c89d603","f48ace1ba734e4bad2c43b13ab6f836cadcc592420802b91b1af5ffd16491455" )
				.when().post(" http://10.0.1.86/snl/oauth/token").asString();
		
         JsonPath path=new JsonPath(token);
         accessToken=path.getString("access_token");	
         System.out.println(accessToken);
	}
	
	@Test(priority=1)
	public void testBoardList_GET(){
		
		RestAssured.given().when().get("http://10.0.1.86/snl/rest/v3/board.json").then().assertThat().statusCode(401);
		RestAssured.given().param("response").auth().oauth2(accessToken).expect().statusCode(200).when().get("http://10.0.1.86/snl/rest/v3/board.json");
		
	}
	
	@Test(priority=0)
	public void testCreateNewBoard_GET() throws Exception{
		
		RestAssured.given().when().get("http://10.0.1.86/snl/rest/v3/board/new.json").then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).expect().body("response.board.turn",equalTo(1))
		.when().get("http://10.0.1.86/snl/rest/v3/board/new.json").then().assertThat().statusCode(200);
		JsonPath res =RestAssured.given().auth().oauth2(accessToken).when().get("http://10.0.1.86/snl/rest/v3/board/new.json").then().extract().jsonPath();
		 boardID=res.getString("response.board.id");
		pathParam=boardID.concat(".json"); 
		System.out.println("Board id is  "+boardID);
		
	}
	
	@Test(priority=2)
	public void testNewBoardWithID_GET(){
		
		RestAssured.given().when().get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).param("response.board.id", boardID).when().get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(200);
		RestAssured.given().auth().oauth2(accessToken).expect().body("response.board.players.size()", equalTo(0)).when().get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(200);
		
	}
	
	@Test(priority=3)
	public void testNewBoardWithID_PUT(){
		
		RestAssured.given().when().put("http://10.0.1.86/snl/rest/v2/board/{id}",pathParam).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).when().put("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(200);

		JsonPath res =RestAssured.given().auth().oauth2(accessToken).when().put("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().extract().jsonPath();
		 String players=res.getString("response.board.players");
		 System.out.println(players);
		 assertEquals(players, null);;
	}
	
	@Test(priority=10)
	public void testNewBoardWithID_DELETE(){
		
		RestAssured.given().when().delete("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).parameters("success", "OK").when()
		.delete("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(200);
		RestAssured.given().auth().oauth2(accessToken).expect().body("response.message", containsString("Invalid board id")).when()
		.get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam);
		RestAssured.given().auth().oauth2(accessToken).expect().body("response.status", equalTo(-1)).when().get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam);
	} 
	
	@Test(priority=4)
	public void testPlayer_POST() throws Exception{
	
		
		JSONObject board=new JSONObject();
		board.put("board", boardID);
		JSONObject player=new JSONObject();
		player.put("name", "TestPlayer");
		board.put("player", player);
		RestAssured.expect().when().post("http://10.0.1.86/snl/rest/v3/player.json").then().assertThat().statusCode(401);
		Response response=RestAssured.given().auth().oauth2(accessToken).body(board).when().post("http://10.0.1.86/snl/rest/v3/player.json").then().extract().response();
		response.then().assertThat().statusCode(200);
		
		JsonPath jsonpath =response.then().extract().jsonPath();
		 playerID=jsonpath.getString("response.player.id");
		 System.out.println(playerID);
		 
		 JsonPath path= RestAssured.given().auth().oauth2(accessToken).expect().when().get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().extract().jsonPath();
		String actualPlayerID=path.getString("response.board.players[0].id");
		 playerName=path.getString("response.board.players[0].name");
		assertEquals(actualPlayerID,playerID);
		assertEquals(playerName,"TestPlayer"); 
		
	}
	
	@Test(priority=6)
	public void testPlayerWithID_GET(){
		
		playerPath=playerID.concat(".json");
		RestAssured.expect().when().get("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).parameters("response.player.id", playerID, "response.player.board_id",boardID,"response.player.name","TestPlayer").when().
		get("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(200);
	}
	
	@Test(priority=7)
	public void testPlayerWithID_PUT(){
		
		RestAssured.expect().when().put("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(401);
		JSONObject player=new JSONObject();
		JSONObject name=new JSONObject();
		name.put("name", "Player1");
		player.put("player", name);
		RestAssured.given().auth().oauth2(accessToken).body(player).when().put("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(200);
		
		JsonPath path= RestAssured.given().auth().oauth2(accessToken).expect().when().get("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().extract().jsonPath();
		playerName=path.getString("response.player.name");
		assertEquals(playerName,"Player1");
	}
	
	@Test(priority=9)
	public void testPlayerWithID_DELETE(){
		
		RestAssured.expect().when().delete("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).when().delete("http://10.0.1.86/snl/rest/v3/player/{id}",playerPath).then().assertThat().statusCode(200);
		RestAssured.given().auth().oauth2(accessToken).parameters("response.board.id",boardID, "response.board.players", null).when()
		.get("http://10.0.1.86/snl/rest/v3/board/{id}",pathParam).then().assertThat().statusCode(200);
	}
	
	@Test(priority=8)
	public void testMove_GET(){
		
		RestAssured.given().expect().when().get("http://10.0.1.86/snl/rest/v3/move/{boardid}?player_id={player_id}",pathParam,playerID).then().assertThat().statusCode(401);
		RestAssured.given().auth().oauth2(accessToken).parameters("response.player.id", playerID, "response.player.board_id",boardID,"response.player.name",playerName)
		.when().get("http://10.0.1.86/snl/rest/v3/move/{boardid}?player_id={player_id}",pathParam,playerID).then().assertThat().statusCode(200);
		
		RestAssured.given().auth().oauth2(accessToken).expect().body("response.roll",lessThan(7),"response.player.position",lessThan(26)).when()
		.get("http://10.0.1.86/snl/rest/v3/move/{boardid}?player_id={player_id}",pathParam,playerID);
		
	}
	

}