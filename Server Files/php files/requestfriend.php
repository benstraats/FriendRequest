<?php
        include 'encrypt.php';

        $user = $_POST["user"];
        $fuser = $_POST["fuser"];
        $pass = $_POST["pass"];

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        //user, fuser, fuser, user
        $requestCheckSQL = "SELECT COUNT(*) FROM requests WHERE (requester=? and requestee=?) or (requester=? and requestee=?)";
        $stmt->prepare($requestCheckSQL);
        $stmt->bind_param('ssss', $user, $fuser, $fuser, $user);
        $stmt->execute();
        $stmt->bind_result($rCount);
        $stmt->fetch();

        //user, fuser, fuser, user
        $friendCheckSQL = "SELECT COUNT(*) FROM friends WHERE (user1=? and user2=?) or (user1=? and user2=?)";
        $stmt->prepare($friendCheckSQL);
        $stmt->bind_param('ssss', $user, $fuser, $fuser, $user);
        $stmt->execute();
        $stmt->bind_result($fCount);
        $stmt->fetch();

        //user, fuser
        $userCheckSQL = "SELECT COUNT(*) FROM users WHERE username=? or username=?";
        $stmt->prepare($userCheckSQL);
        $stmt->bind_param('ss', $user, $fuser);
        $stmt->execute();
        $stmt->bind_result($uCount);
        $stmt->fetch();

        if ($rCount == 0 && $fCount == 0 && $uCount == 2 && $user != $fuser) {

                //user
                $passCheckSQL = "SELECT password FROM users WHERE username=?";
                $stmt->prepare($passCheckSQL);
                $stmt->bind_param('s', $user);
                $stmt->execute();
                $stmt->bind_result($passResult);
                $stmt->fetch();

                if (pass_check($pass, $passResult)) {

                        //fuser, user
                        $insertSQL = "INSERT INTO requests (requestee, requester) VALUES (?,?)";
                        $stmt->prepare($insertSQL);
                        $stmt->bind_param('ss', $fuser, $user);
                        $stmt->execute();

                        echo "Friend Request Sent To " . $fuser;
                }
        }
        mysqli_close($con);
?>
