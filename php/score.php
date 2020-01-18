<?php
	//receive string encoded
	$in = file_get_contents('php://input');
	//decode json string
	$json = json_decode($in);
	
	//Check json object received
	if(!isset($json->game)){
		echo "CAN'T FIND THE GAME";
		die;
	}
	
	if(!isset($json->cards)){
		echo "CAN'T FIND  CARDS";
		die;
	}
	
	if(!is_array($json->cards)){
		echo "INVALID CARDS TYPE";
		die;
	}
	
	foreach($json->cards as $i=>$card){
		//TODO: Check if suits are ok
		if(!isset($card->suit)){
			echo "CARD AT ".$i." MISSING SUIT";
			die;
		}			
		if( !isset($card->number)){
			echo "CARD AT ".$i." MISSING NUMBER";
			die;
		}
	}
	
	// games/scopa.php
	if(!file_exists("games/$json->game.php")){
		echo "THE GAME DOES NOT EXIST";
		die;
	}		

	include("games/$json->game.php");

	//suits: "CLUB","HEART","DIAMOND","SPADE"
	//number: 1-13, if jolly 14
	
	echo getPoints($json->cards);
	
//	$in = '{
//		"game": "scala",
//		"cards": [
//			{
//				"suit": "picche",
//				"number": 1
//			},
//			{
//				"suit": "fiori",
//				"number": 2		
//			},
//			{
//				"suit": "cuori",
//				"number": 3		
//			},
//			{
//				"suit": "quadri",
//				"number": 3		
//			},
//			{
//				"suit": "fiori",
//				"number": 4		
//			},
//			{
//				"suit": "quadri",
//				"number": 7	
//			},
//			{
//				"suit": "picche",
//				"number": 10		
//			},
//			{
//				"suit": "cuori",
//				"number": 12		
//			},
//			{
//				"suit": "fiori",
//				"number": 12		
//			},
//			{
//				"suit": "quadri",
//				"number": 13		
//			}
//		]
//	}';
	
?>
