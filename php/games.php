<?php
	$files = scandir("./games");
	
	foreach($files as $file){
		if(!is_dir($file)){
			$info = pathinfo($file);
			if($info['extension'] == "php"){
				echo $info['filename']."<br>";
			}
		}
	}
	
?>