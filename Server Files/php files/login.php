<?php
        include 'encrypt.php';

        $user = $_POST["user"];
        $pass = $_POST["password"];

        // Create connection
        $con = new mysqli("localhost","fradmin","people123","friendrequest");
        $stmt = $con->stmt_init();

        //user
        $passSQL = "SELECT password FROM users WHERE username=?";
        $stmt->prepare($passSQL);
        $stmt->bind_param('s', $user);
        $stmt->execute();
        $stmt->bind_result($pass_return);
        $stmt->fetch();

        if(pass_check($pass, $pass_return)) {
                echo "True";
        }
        else {
                echo "False";
        }

        mysqli_close($con);

?>