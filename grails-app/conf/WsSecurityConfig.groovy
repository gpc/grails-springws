/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class WsSecurityConfig {

    static order = 1

    def myKeyStore

    def validation = {
        // This?
//        actor = 'abc'
        // or this, or both?
//        actor 'abc'

        actions{
            usernameToken(users:['Gort':'Klaatu barada nikto'])
//            usernameToken()
//            timestamp()
        }
    }

    def securement = {
//        actions{
//            signature(keyStore:myKeyStore, keyAlias:'mykey', keyPassword:'123456')
//        }
    }
}
