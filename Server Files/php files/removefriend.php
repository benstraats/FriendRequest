<?PHP

        include 'encrypt.php';

        //user, currUser. currPass
        $user = $_POST["user"];
        $currUser = $_POST["currUser"];
        $currPass = $_POST["currPass"];

        $con = new mysqli("localhost", "fradmin", "people123", "friendrequest");

        $user_pass_sql = "SELECT password FROM users WHERE username=?";
        $stmt = $con->stmt_init();
        $stmt->prepare($user_pass_sql);
        $stmt->bind_param('s', $currUser);
        $stmt->execute();
        $stmt->bind_result($user_pass);
        $stmt->fetch();

        $friend_count_sql = "SELECT COUNT(*) FROM friends WHERE (user1=? and user2=?) or (user1=? and user2=?)";
        $stmt->prepare($friend_count_sql);
        $stmt->bind_param('ssss', $user, $currUser, $currUser, $user);
        $stmt->execute();
        $stmt->bind_result($count);
        $stmt->fetch();

        if(pass_check($currPass, $user_pass) && $count > 0) {

                $delete_friend_sql = "DELETE FROM friends WHERE (user1=? and user2=?) or (user1=? and user2=?)";
                $stmt->prepare($delete_friend_sql);
                $stmt->bind_param('ssss', $user, $currUser, $currUser, $user);
                $stmt->execute();

                echo "true";

        }

        else {
                echo "false";
        }

?>