<?php

        $user = $_POST["user"];
        $search = $_POST["search"];
        $index = $_POST["index"];

        $count = 0;

        // Create connection
        $con=mysqli_connect("localhost","fradmin","people123","friendrequest");

        //Do note include users that have requests with this user or are already friends
        $namesearch = mysqli_query($con,"SELECT username, name FROM users WHERE (username LIKE '%" . $search . "%' or name LIKE '%" . $search ."%') and username NOT IN (SELECT requestee AS user FROM requests WHERE requester='" . $user . "' UNION SELECT requester AS user FROM requests WHERE requestee='" . $user . "' UNION SELECT user1 AS user FROM friends WHERE user2='" . $user . "' UNION SELECT user2 AS user FROM friends WHERE user1='" . $user . "') ORDER BY name, username LIMIT " . $index . ", 21");

        while($row = mysqli_fetch_array($namesearch)) {
                if ($count < 20) {
                        echo $row['username'] . "," . $row['name'] . ";";
                }
                $count = $count + 1;
        }

        //This will only happen if there are no more entries after the 20 we returned
        if ($count != 21){
                echo "\n";
        }

        mysqli_close($con);

?>