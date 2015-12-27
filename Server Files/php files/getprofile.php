<?php
        include 'encrypt.php';

        //this file ugly AF
        $user = $_POST["user"];
        $currUser = $_POST["currUser"];
        $currPass = $_POST["currPass"];

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        //If its to view their own profile
        if ($user == $currUser) {

                //user
                $passCheckSQL = "SELECT password FROM users WHERE username=?";
                $stmt->prepare($passCheckSQL);
                $stmt->bind_param('s', $user);
                $stmt->execute();
                $stmt->bind_result($pass_return1);
                $stmt->fetch();


                if (pass_check($currPass, $pass_return1)) {

                        //user
                        $profileSQL1 = "SELECT field,value FROM profile WHERE user=?";
                        $stmt->prepare($profileSQL1);
                        $stmt->bind_param('s', $user);
                        $stmt->execute();
                        $stmt->bind_result($field1, $value1);

                        while($stmt->fetch()) {

                                $val_decrypt = decryption($value1);

                                echo $field1 . "\t" . $val_decrypt . "\n";
                        }
                }
        }

        else {

                //currUser, user, user, currUser, currUser
                //returns the password of the currUsers profile only if currUser is friends with user.
                $fullCheckSQL = "SELECT u.password FROM users as u JOIN friends as f ON u.username=f.user1 OR u.username=f.user2 WHERE ((f.user1=? and f.user2=?) OR (f.user1=? and f.user2=?)) and u.username=?";
                $stmt->prepare($fullCheckSQL);
                $stmt->bind_param('sssss', $currUser, $user, $user, $currUser, $currUser);
                $stmt->execute();
                $stmt->bind_result($pass_return2);
                $stmt->fetch();

                //will fail if passwords do not match
                if (pass_check($currPass, $pass_return2)){

                        //user
                        $profileSQL2 = "SELECT field,value FROM profile WHERE user=?";
                        $stmt->prepare($profileSQL2);
                        $stmt->bind_param('s', $user);
                        $stmt->execute();
                        $stmt->bind_result($field2, $value2);

                        while($stmt->fetch()) {

                                $val_dec = decryption($value2);
                                echo $field2 . ":" . $val_dec . "\n";
                        }
                }
        }

        mysqli_close($con);
?>
