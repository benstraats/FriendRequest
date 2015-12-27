<?php

        $user = $_POST["user"];

        $con = new mysqli("localhost", "fradmin", "people123", "friendrequest");
        $stmt = $con->stmt_init();

        //user
        $countSQL = "SELECT COUNT(*) FROM requests WHERE requestee=?";
        $stmt->prepare($countSQL);
        $stmt->bind_param('s', $user);
        $stmt->execute();
        $stmt->bind_result($count);
        $stmt->fetch();

        echo $count;

        mysqli_close($con);

?>