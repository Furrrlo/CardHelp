<?php
	$files = scandir("./games");
	
	$arr = array();
	
	foreach($files as $file){
		if(!is_dir($file)){
			$info = pathinfo($file);
			if($info['extension'] == "php"){
				array_push($arr,$info['filename']);
			}
		}
	}
	
	$jsonData = json_encode($arr);

	echo $jsonData;
	
?>
