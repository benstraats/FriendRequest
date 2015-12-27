<?php
        include 'encrypt.php';

        //THIS FILE IS VULNERABLE
        //user can access this directly and spam \n and \t
        //i have no idea how to stop this
        //think i gotta do JSON

        $user = $_POST["user"];
        $pass = $_POST["password"];
        $profile = $_POST["profile"];

        $jsonProfile = json_decode($profile);

        //Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        $count = 1;

        //user
        $passCheckSQL = "SELECT password FROM users WHERE username=?";
        $stmt->prepare($passCheckSQL);
        $stmt->bind_param('s', $user);
        $stmt->execute();
        $stmt->bind_result($pass_result);
        $stmt->fetch();

        //checking the password will fail if user does not exist
        if (pass_check($pass, $pass_result) && !(strpos($profile, "\n") !== false) && !(strpos($profile, "\t") !== false)) {

                //user
                $clearProfileSQL = "DELETE FROM profile WHERE user=?";
                $stmt->prepare($clearProfileSQL);
                $stmt->bind_param('s', $user);
                $stmt->execute();

                foreach($jsonProfile->profile as $row)
                {
                        foreach ($row as $key => $val)
                        {
                                if ($val != "") {
                                        $val_en = encryption($val);

                                        $insertSQL = "INSERT INTO profile VALUES(?,?,?,?)";
                                        $stmt->prepare($insertSQL);
                                        $stmt->bind_param('isss', $count, $user, $key, $val_en);
                                        $stmt->execute();

                                        $count = $count + 1;
                                }
                        }
                        echo "True";
                }
        }
?>
