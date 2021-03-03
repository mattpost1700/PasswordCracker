import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.*;

public class PasswordCracker {
    final static String PASSWORD_FILE_AS_STRING = "src/password.txt";
    final static String CRACKED_PASSWORDS_FILE_AS_STRING = "src/cracked passwords.txt";
    final static String DICTIONARY1_FILE_AS_STRING = "src/dictionary1.txt";//score: 799 ~10 seconds
    final static String DICTIONARY2_FILE_AS_STRING = "src/dictionary2.txt";//score: 1822 ~6 minutes
    final static String DICTIONARY3_FILE_AS_STRING = "src/dictionary3.txt";//score: 1553 ~30 seconds
    final static String DICTIONARY4_FILE_AS_STRING = "src/dictionary4.txt";//score: 1782 ~5 minutes

    HashSet<String> listOfCrackedPasswords = new HashSet<>();
    HashMap<String, String> lookupTable = new HashMap<>();
    Account[] accountsArray = new Account[621];
    ArrayList<String> knownPasswordsList = new ArrayList<>();

    public static void main(String[] args) {
        PasswordCracker pc = new PasswordCracker();

        //NOTE: written answers were only calculated from dictionary2.txt
//        pc.knownPasswordAttack();
//        pc.answerQuestions();
        pc.rootAttack3();

        pc.savePasswordsToFile();
    }

    PasswordCracker() {
        try {
            Scanner origPasswordScanner = new Scanner(new File(PASSWORD_FILE_AS_STRING));
            int i = 0;
            while (origPasswordScanner.hasNext()) {
                String[] tokens = origPasswordScanner.nextLine().split(":");
                accountsArray[i] = new Account(tokens[0], tokens[1], tokens[2]);
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void answerQuestions() {
        HashSet<String> hasUppercase = new HashSet<>();
        HashSet<String> hasLowercase = new HashSet<>();
        HashSet<String> hasNumbers = new HashSet<>();
        HashSet<String> hasPunctuation = new HashSet<>();
        HashSet<String> passwordList = new HashSet<>();
        HashSet<String> atLeastTwoSets = new HashSet<>();
        HashSet<String> atLeastThreeSets = new HashSet<>();
        HashSet<String> atLeastFourSets = new HashSet<>();
        HashSet<String> endingWithOne = new HashSet<>();
        HashSet<String> allLowerCase = new HashSet<>();
        HashSet<String> containsPassword = new HashSet<>();
        HashSet<String> endsWithPeriodOrE = new HashSet<>();

        try {
            Scanner crackedPasswordScanner = new Scanner(new File(CRACKED_PASSWORDS_FILE_AS_STRING));
            while (crackedPasswordScanner.hasNext()) {
                String[] tokens = crackedPasswordScanner.nextLine().split(" ");
                String password = tokens[1];
                passwordList.add(password);
                int numOfSets = 0;
                boolean lower = false, upper = false, punc = false, num = false;
                for (int i = 0; i < password.length(); i++) {
                    char c = password.charAt(i);

                    if (c >= 97 && c <= 122) {//lowercase
                        hasLowercase.add(password);
                        lower = true;
                    }
                    if (c >= 65 && c <= 90) {//uppercase
                        hasUppercase.add(password);
                        upper = true;
                    }
                    if (c >= 49 && c <= 57) {//numbers
                        hasNumbers.add(password);
                        num = true;
                    }
                    if ((c >= 33 && c <= 47) || (c >= 58 && c <= 64) || (c >= 91 && c <= 96) || (c >= 123 && c <= 126)) {//punct.
                        hasPunctuation.add(password);
                        punc = true;
                    }
                }

                if (lower)
                    numOfSets++;
                if (upper)
                    numOfSets++;
                if (punc)
                    numOfSets++;
                if (num)
                    numOfSets++;

                if (numOfSets >= 2) {
                    atLeastTwoSets.add(password);
                }
                if (numOfSets >= 3) {
                    atLeastThreeSets.add(password);
                }
                if (numOfSets >= 4) {
                    atLeastFourSets.add(password);
                }
                if (password.charAt(password.length() - 1) == '1')
                    endingWithOne.add(password);
                if (password.toLowerCase().equals(password))
                    allLowerCase.add(password);
                if (password.contains("password"))
                    containsPassword.add(password);
                if (password.charAt(password.length() - 1) == '!' || password.charAt(password.length() - 1) == '.')
                    endsWithPeriodOrE.add(password);

            }

            System.out.println("Number of passwords: " + passwordList.size());
            System.out.println("a) Longest password found: " + longestWord(passwordList));
            System.out.println("b) Longest password in at least 2 sets: " + longestWord(atLeastTwoSets));
            System.out.println("c) Longest password in at least 3 sets: " + longestWord(atLeastThreeSets));
            System.out.println("d) Longest password in all 4 sets: " + longestWord(atLeastFourSets));
            System.out.println("e) ends in '1': " + endingWithOne.size());
            System.out.println("e) all lowercase: " + allLowerCase.size());
            System.out.println("e) contains password: " + containsPassword.size());
            System.out.println("e) ends with ! or .: " + endsWithPeriodOrE.size());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String longestWord(HashSet<String> list) {
        String longestWord = "";
        for (String element : list) {
            if (element.length() > longestWord.length()) {
                longestWord = element;
            }
        }
        if (longestWord.equals(""))
            longestWord = "No password found.";
        return longestWord;
    }

    void checkDictionary(String filePath) {
        try {
            Scanner dictionaryScanner = new Scanner(new File(filePath));
            while (dictionaryScanner.hasNext()) {
                String password = dictionaryScanner.next();
                for (Account a : accountsArray) {
                    a.testPassword(password);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addKnownPasswords() {
        addPass("password", "1234", "password1");

        checkDictionary(DICTIONARY4_FILE_AS_STRING);
        checkDictionary(DICTIONARY3_FILE_AS_STRING);
        checkDictionary(DICTIONARY2_FILE_AS_STRING);//only needed password
        checkDictionary(DICTIONARY1_FILE_AS_STRING);
    }

    void addPass(String... arr) {
        for (String s : arr) {
            knownPasswordsList.add(s);
        }
    }

    void knownPasswordAttack() {
        addKnownPasswords();
        for (Account a : accountsArray) {
            for (String password : knownPasswordsList) {
                if (a.testPassword(password))
                    break;
            }
        }
    }

    void createLookupTable() {
        String s = "";
        int lower_limit = 97;//97
        int upper_limit = 122;//122
        char[] guess = new char[10];
        for (int a = lower_limit; a <= upper_limit; a++) {
            for (int b = lower_limit; b <= upper_limit; b++) {
                for (int c = lower_limit; c <= upper_limit; c++) {
                    for (int d = lower_limit; d <= upper_limit; d++) {
                        for (int e = lower_limit; e <= upper_limit; e++) {
                            for (int f = lower_limit; f <= upper_limit; f++) {
                                for (int g = lower_limit; g <= upper_limit; g++) {
                                    for (int h = lower_limit; h <= upper_limit; h++) {
                                        guess[0] = (char) a;
                                        guess[1] = (char) b;
                                        guess[2] = (char) c;
                                        guess[3] = (char) d;
                                        guess[4] = (char) e;
                                        guess[5] = (char) f;
                                        guess[6] = (char) g;
                                        guess[7] = (char) h;
                                        s = getHash(new String(guess));
                                        lookupTable.put(s, new String(guess));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.print(lookupTable.get(s));
    }

    void guessAttack(String s) {
        for (Account a : accountsArray) {
            a.testPassword(s);
        }
    }

    void rootAttack3() {
        String one = "Dr";
        String two = "8Lum444";
        for(int i = 0; i < 200; i++){
            accountsArray[0].testPassword(one + (char) i + two);
        }
    }

    //**
    // Checks 1001 hashes of password in dictionary2
    // */
    void rootAttack2() {
        Account rootAccount = accountsArray[0];

        try {
            Scanner dictionaryScanner = new Scanner(new File(DICTIONARY2_FILE_AS_STRING));
            while (dictionaryScanner.hasNext()) {
                String password = dictionaryScanner.next();
                for (int i = 0; i < 1001; i++) {
                    if (rootAccount.testPassword(password)) {
                        System.err.println("Root password: " + password);
                        System.exit(-1);
                    }
                    password = getHash(password);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void rootAttack1() {
        Account rootAccount = accountsArray[0];
        String rootHash = rootAccount.hash;

        createLookupTable();
        rootAccount.testPassword(lookupTable.get(rootHash));
    }

    void pureBruteForceAttack() {
        int lower_limit = 97;
        int upper_limit = 122;
        char[] guess = new char[8];
        for (Account account : accountsArray) {
            for (int a = lower_limit; a <= upper_limit; a++) {
                for (int b = lower_limit; b <= upper_limit; b++) {
                    for (int c = lower_limit; c <= upper_limit; c++) {
                        for (int d = lower_limit; d <= upper_limit; d++) {
                            for (int e = lower_limit; e <= upper_limit; e++) {
                                for (int f = lower_limit; f <= upper_limit; f++) {
                                    for (int g = lower_limit; g <= upper_limit; g++) {
                                        for (int h = lower_limit; h <= upper_limit; h++) {
                                            guess[0] = (char) a;
                                            guess[1] = (char) b;
                                            guess[2] = (char) c;
                                            guess[3] = (char) d;
                                            guess[4] = (char) e;
                                            guess[5] = (char) f;
                                            guess[6] = (char) g;
                                            guess[7] = (char) h;
                                            account.testPassword(new String(guess));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void accountNameAttack() {
        for (Account a : accountsArray) {
            a.testPassword(a.accountName);
        }
    }

    void savePasswordsToFile() {
        String accountsAsString = "";
        int score = 0;
//        Object[] accountsAsArray = listOfCrackedPasswords.toArray();
//        for(Object account: accountsAsArray) {
//            accountsAsString += (String) account + '\n';
//        }
        for (Account a : accountsArray) {
            if (a.password != null) {
                accountsAsString += a + "\n";
                //couldn't use first letter because root and another user starts with 'r'
                switch (a.accountName.charAt(1)) {
                    case 'o'://root
                        score += 1000;
                        break;
                    case 'd'://admin
                        score += 50;
                        break;
                    case 'e'://celeb
                        score += 10;
                        break;
                    default://other user
                        score += 1;
                        break;
                }
            }
        }

        try {
            FileWriter crackedPasswordWriter = new FileWriter(CRACKED_PASSWORDS_FILE_AS_STRING);
            crackedPasswordWriter.write(accountsAsString);
            crackedPasswordWriter.close();
            System.out.println("Current score: " + score);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getHash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class Account {
        String accountName;
        String salt;
        String hash;
        String password;

        Account(String accountName, String salt, String hash) {
            this.accountName = accountName;
            this.salt = salt;
            this.hash = hash;
            password = null;
        }

        boolean testPassword(String passwordParameter) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String generatedHash = Base64.getEncoder().encodeToString(md.digest((salt + passwordParameter).getBytes()));
                if (generatedHash.equals(this.hash)) {//correct password
                    password = passwordParameter;
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        public String toString() {
            return accountName + " " + password;
        }

        public String detailedToString() {
            return accountName + ":" + salt + ":" + hash;
        }
    }
}
