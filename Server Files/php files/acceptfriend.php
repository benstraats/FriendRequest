<?php

        include 'encrypt.php';

        $user = $_POST["user"];;
        $pass = $_POST["pass"];
        $fuser = $_POST["fuser"];

        // Create connection
        $con=new mysqli("localhost","fradmin","people123","friendrequest");

        //user, fuser, fuser, user
        $friendCountSQL = "SELECT COUNT(*) FROM friends WHERE (user1=? and user2=?) or (user1=? and user2=?)";
        $stmt = $con->stmt_init();
        $stmt->prepare($friendCountSQL);
        $stmt->bind_param('ssss', $user, $fuser, $fuser, $user);
        $stmt->execute();
        $stmt->bind_result($count);
        $stmt->fetch();

        //user, fuser
        $user_c_sql = "SELECT COUNT(*) as c FROM users WHERE username=? or username=?";
        $stmt->prepare($user_c_sql);
        $stmt->bind_param('ss', $user, $fuser);
        $stmt->execute();
        $stmt->bind_result($user_count);
        $stmt->fetch();

        if ($count == 0 && $user_count == 2) {

                //user
                $user_pass_sql = "SELECT password FROM users WHERE username=?";
                $stmt->prepare($user_pass_sql);
                $stmt->bind_param('s', $user);
                $stmt->execute();
                $stmt->bind_result($pass_return);
                $stmt->fetch();

                if(pass_check($pass, $pass_return)) {

                        //now check that the request is actually there
                        //user, fuser
                        $request_sql = "SELECT COUNT(*) FROM requests WHERE requestee=? and requester=?";
                        $stmt->prepare($request_sql);
                        $stmt->bind_param('ss', $user, $fuser);
                        $stmt->execute();
                        $stmt->bind_result($r_count);
                        $stmt->fetch();

                        if ($r_count == 1) {

                                //user, fuser
                                $delete_sql = "DELETE FROM requests WHERE requestee=? and requester=?";
                                $stmt->prepare($delete_sql);
                                $stmt->bind_param('ss', $user, $fuser);
                                $stmt->execute();

                                //user, fuser
                                $insert_sql = "INSERT INTO friends (user1, user2) VALUES (?,?)";
                                $stmt->prepare($insert_sql);
                                $stmt->bind_param('ss', $user, $fuser);
                                $stmt->execute();

                                echo "Friend Request Accepted For " . $fuser;
                        }
                }
        }
        mysqli_close($con);
?>