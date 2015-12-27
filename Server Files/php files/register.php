<?php
        include 'encrypt.php';

        $user = $_POST["user"];
        $name = $_POST["name"];
        $password = $_POST["password"];

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        //user
        $userCheckSQL = "SELECT COUNT(*) FROM users WHERE username=?";
        $stmt->prepare($userCheckSQL);
        $stmt->bind_param('s', $user);
        $stmt->execute();
        $stmt->bind_result($count);
        $stmt->fetch();

        //todo fix regex's. name=all loanguage chars and '-{space}
        //username: capitols and lowercase lagnauge and 0-9
        //$namePattern = "^([ \u00c0-\u01ffa-zA-Z'-])+$";
        //$userPattern = "^[A-Za-z0-9]+$";

        //check that name and username dont contain ',' or ';'
        if($count != 0 || strpos($user, ',') !== false || strpos($user, ';') !== false || strpos($name, ';') !== false || strpos($name, ',') !== false) {
                echo "False";
        }

        //more regex
        //removed: !preg_match($user, $userPattern) || !preg_match($name, $namePattern)
        //since regex's are broken
        else if(strpos($user, "\t") !== false || strpos($user, "\n") !== false || strpos($name, "\t") !== false || strpos($name, "\n") !== false || strlen($user) > 30 || strlen($user) < 3 || strlen($name) > 50 || strlen($name) < 3 || strlen($password) > 30 || strlen($password) < 6) {
                echo "False";//second regex check
        }

        else {
                $pass_en = pass_enc($password);

                //user, name, pass_en
                //Insert the data
                $insertSQL = "INSERT INTO users (username, name, password) VALUES (?,?,?)";
                $stmt->prepare($insertSQL);
                $stmt->bind_param('sss', $user, $name, $pass_en);
                $stmt->execute();

                echo "True";
        }

        mysqli_close($con);

?>