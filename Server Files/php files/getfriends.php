<?php

        $user = $_POST["user"];
        $index = $_POST["index"];

        $count = 0;

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");

        //user, user, index
        //return by username,name;
        $friendSQL = "SELECT username, name FROM users WHERE username IN (SELECT user1 AS user FROM friends WHERE user2=? UNION SELECT user2 AS user FROM friends WHERE user1=?) ORDER BY name, username LIMIT ?, 21";

        $stmt = $con->stmt_init();
        $stmt->prepare($friendSQL);
        $stmt->bind_param('ssi', $user, $user, $index);
        $stmt->execute();
        $stmt->bind_result($username, $names);

        while($stmt->fetch()) {

                if ($count < 20) {
                        echo $username . "," . $names . ";";
                }
                $count = $count + 1;
        }

        //This will only happen if there are no more entries after the 20 we returned
        if ($count != 21) {
                echo "\n";
        }

        mysqli_close($con);

?>
