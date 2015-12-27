<?php

        $user = $_POST["user"];
        $index = $_POST["index"];

        $count = 0;

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        //Searches by name then username(creates it to be unique
        //user, index
        $requesterSQL = "SELECT username, name FROM users WHERE username IN (SELECT requester AS username FROM requests WHERE requestee=?) ORDER BY name, username LIMIT ?, 21";
        $stmt->prepare($requesterSQL);
        $stmt->bind_param('si', $user, $index);
        $stmt->execute();
        $stmt->bind_result($username, $names);

        while($stmt->fetch()) {
                if ($count < 20) {
                        echo $username . "," . $names . ";";
                }
                $count = $count + 1;
        }

        if ($count != 21) {
                echo "\n";
        }

        mysqli_close($con);

?>