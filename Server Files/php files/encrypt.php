<?PHP

        function pass_enc($pass) {
                return password_hash($pass, PASSWORD_BCRYPT);
        }

        function pass_check($pass, $pass_enc) {
                return password_verify($pass, $pass_enc);
        }

        function encryption($value) {

                $iv = mcrypt_create_iv(mcrypt_get_iv_size(MCRYPT_RIJNDAEL_256, MCRYPT_MODE_CBC), MCRYPT_DEV_URANDOM);

                //Get better key?
                $secret_key = '53873A716DD4DB89A4B2B44540E5E71481013F4F1397DD62179EE65E87D1D679';
                $encrypt = serialize($value);

                $key_pack = pack('H*', $secret_key);
                $mac = hash_hmac('sha256', $encrypt, substr(bin2hex($key_pack), -32));
                $passcrypt = mcrypt_encrypt(MCRYPT_RIJNDAEL_256, $key_pack, $encrypt.$mac, MCRYPT_MODE_CBC, $iv);

                $encode = base64_encode($passcrypt).'|'.base64_encode($iv);

                return $encode;
        }

        function decryption($decrypt) {

                //Get better key?
                $secret_key = '53873A716DD4DB89A4B2B44540E5E71481013F4F1397DD62179EE65E87D1D679';

                $decrypt = explode('|', $decrypt.'|');
                $decoded = base64_decode($decrypt[0]);
                $iv = base64_decode($decrypt[1]);

                if(strlen($iv)!==mcrypt_get_iv_size(MCRYPT_RIJNDAEL_256, MCRYPT_MODE_CBC)){ return false; }

                $key = pack('H*', $secret_key);

                $decrypted = trim(mcrypt_decrypt(MCRYPT_RIJNDAEL_256, $key, $decoded, MCRYPT_MODE_CBC, $iv));
                $mac = substr($decrypted, -64);
                $decrypted = substr($decrypted, 0, -64);

                $calcmac = hash_hmac('sha256', $decrypted, substr(bin2hex($key), -32));
                if($calcmac!==$mac){ return false; }

                $decrypted = unserialize($decrypted);
                return $decrypted;

        }
?>