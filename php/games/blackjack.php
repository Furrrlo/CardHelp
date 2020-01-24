<?php
function getPoints($cards) {

	$score = 0;
	$count = 0;
	foreach($cards as $i=>$card){

	if($score < 21)
	{	
		count++;

		if(($card->number) == 1)
		{
			$scoreSpade = $score + 10;
			$score = $score + 1;
		}
		else if(($card->number) > 11){
		$score = 10 + ($card->number); 
		}
		else
		{
			$score = $score + ($card->number);
		}	

		if($count <2 && ($score == 21 || $scoreSpade == 21) )
		{
			return "BLACKJACK!"
		}

		
	return $score."-".$scoreSpade;
	}
	else
	{
		return "HAI SBALLATO";
	}

	}
?>