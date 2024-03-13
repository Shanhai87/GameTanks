package edu.school21.game.server;

import edu.school21.game.config.SocketApplicationConfig;
import edu.school21.game.models.GameRoom;
import edu.school21.game.models.Shell;
import edu.school21.game.models.User;
import edu.school21.game.services.UsersService;
import edu.school21.game.services.UsersServiceImpl;
import org.json.JSONObject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed())
                new ClientHandler(serverSocket.accept()).start();
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    public static class ClientHandler extends Thread {
        private static final List<ClientHandler> clientHandlers = new ArrayList<>();
        private static final List<ClientHandler> readyHandlers = new ArrayList<>();
        private final Socket clientSocket;
        private final PrintWriter outStream;
        private final BufferedReader inStream;
        private UsersService usersService;
        private volatile GameRoom room;
        private volatile User user;
        private volatile User enemy;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            outStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            user = null;
            enemy = null;
            room = null;
        }

        public void run() {
            try {
                AnnotationConfigApplicationContext applicationContext =
                        new AnnotationConfigApplicationContext(SocketApplicationConfig.class);
                usersService = applicationContext.getBean(UsersServiceImpl.class);
                String message;
                while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                    message = inStream.readLine();
                    if (message == null) {
                        System.out.println((String) null);
                        break;
                    }
                    JSONObject json = new JSONObject(message);
                    if (json.getString("stage").equals("exit")) {
                        break;
                    } else if (json.getString("stage").equals("startGame")) {
                        messageForClient(room);
                        if (!playGame()) {
                            break;
                        }
                    } else if (json.getString("stage").equals("signIn")) {
                        signIn(json);
                    } else if (json.getString("stage").equals("signUp")) {
                        signUp(json);
                    } else if (json.getString("stage").equals("ready")) {
                        enemy = null;
                        room = null;
                        user.setStatus("READY");
                        usersService.updateUser(user);
                        createGameRoom();
                    }
                }
                user.setStatus("OFFLINE");
                usersService.updateUser(user);
                readyHandlers.remove(this);
                clientHandlers.remove(this);
                applicationContext.close();
                clientSocket.close();
            } catch (IOException ignore) {
            }
        }

        private void signIn(JSONObject json) {
            String username = json.getString("username");
            String password = json.getString("password");
            json = new JSONObject();
            json.put("stage", "signIn");
            if (usersService.checkUser(username, password)) {
                Optional<User> optionalUser = usersService.getUserByName(username);
                optionalUser.ifPresent(value -> user = value);
                if (user.getStatus().equals("OFFLINE")) {
                    clientHandlers.add(this);
                    json.put("status", "true");
                    user.setStatus("ONLINE");
                    usersService.updateUser(user);
                } else {
                    json.put("status", "false");
                }
            } else {
                json.put("status", "false");
            }
            outStream.println(json.toString().replaceAll("\"", "\\\""));
        }

        private void signUp(JSONObject json) {
            String username = json.getString("username");
            String password = json.getString("password");
            json = new JSONObject();
            json.put("stage", "signUp");
            if (usersService.getUserByName(username).isEmpty()) {
                usersService.signUp(username, password);
                json.put("status", "true");
            } else {
                json.put("status", "false");
            }
            outStream.println(json.toString().replaceAll("\"", "\\\""));
        }

        private void exitFromActiveGame(JSONObject json) {
            user.setStatus("OFFLINE");
            enemy.setStatus("ONLINE");
            int[] array;
            if (user.equals(room.getUser1())) {
                enemy.setHits(enemy.getHits() + room.getHits2());
                array = new int[]{room.getHits2(), room.getMisses2(), room.getUser2().getHits(),
                        room.getUser2().getMisses()};
            } else {
                enemy.setHits(enemy.getHits() + room.getHits1());
                array = new int[]{room.getHits1(), room.getMisses1(), room.getUser1().getHits(),
                        room.getUser1().getMisses()};
            }
            usersService.updateUser(user);
            usersService.updateUser(enemy);
            json = new JSONObject();
            json.put("stage", "newGame");
            json.put("isWin", "true");
            json.put("stats", array);
            for (ClientHandler handler : clientHandlers) {
                if (handler.user.equals(enemy)) {
                    handler.outStream.println(json.toString().replaceAll("\"", "\\\""));
                }
            }
        }

        private boolean playGame() {
            TimerTask task1 = null;
            TimerTask task2 = null;
            Timer timer = new Timer();
            while (!clientSocket.isClosed() && user.getStatus().equals("INGAME")) {
                try {
                    String message = inStream.readLine();
                    JSONObject json = new JSONObject(message);
                    if (json.getString("stage").equals("exit")) {
                        exitFromActiveGame(json);
                        stopTask(task1, task2, timer);
                        return false;
                    } else if (json.getString("stage").equals("inGame")) {
                        if (user.equals(room.getUser1())) {
                            if (json.getString("command").equals("shoot")) {
                                room.getShellList1().add(new Shell(room.getX1(), room.getY1()));
                            } else if (json.getString("command").equals("right")) {
                                task1 = new TimerTask() {
                                    public void run() {
                                        room.setX1(room.getX1() + 10);
                                        if (room.getX1() > 590) {
                                            room.setX1(590);
                                        }
                                    }
                                };
                                timer.schedule(task1, 250);
                            } else if (json.getString("command").equals("left")) {
                                task2 = new TimerTask() {
                                    public void run() {
                                        room.setX1(room.getX1() - 10);
                                        if (room.getX1() < 0) {
                                            room.setX1(0);
                                        }
                                    }
                                };
                                timer.schedule(task2, 250);
                            } else if (json.getString("command").equals("stop")) {
                                if (task1 != null) task1.cancel();
                                if (task2 != null) task2.cancel();
                            }
                        } else if (user.equals(room.getUser2())) {
                            if (json.getString("command").equals("shoot")) {
                                room.getShellList2().add(new Shell(room.getX2(), room.getY2()));
                            } else if (json.getString("command").equals("right")) {
                                task1 = new TimerTask() {
                                    public void run() {
                                        room.setX2(room.getX2() + 10);
                                        if (room.getX2() > 590) {
                                            room.setX2(590);
                                        }
                                    }
                                };
                                timer.schedule(task1, 250);
                            } else if (json.getString("command").equals("left")) {
                                task2 = new TimerTask() {
                                    public void run() {
                                        room.setX2(room.getX2() - 10);
                                        if (room.getX2() < 0) {
                                            room.setX2(0);
                                        }
                                    }
                                };
                                timer.schedule(task2, 250);
                            } else if (json.getString("command").equals("stop")) {
                                if (task1 != null) task1.cancel();
                                if (task2 != null) task2.cancel();
                            }
                        }
                    }
                } catch (IOException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            stopTask(task1, task2, timer);
            return true;
        }

        private void stopTask(TimerTask task1, TimerTask task2, Timer timer) {
            if (task1 != null) task1.cancel();
            if (task2 != null) task2.cancel();
            timer.cancel();
            timer.purge();
        }

        private void createGameRoom() throws IOException {
            if (!readyHandlers.isEmpty()) {
                ClientHandler handlerEnemy = readyHandlers.getFirst();
                readyHandlers.remove(handlerEnemy);
                enemy = handlerEnemy.user;
                handlerEnemy.enemy = user;
                user.setStatus("INGAME");
                enemy.setStatus("INGAME");
                usersService.updateUser(user);
                usersService.updateUser(enemy);
                room = new GameRoom(user, enemy);
                handlerEnemy.room = room;
                JSONObject json = new JSONObject();
                json.put("stage", "startGame");
                String message = json.toString().replaceAll("\"", "\\\"");
                handlerEnemy.outStream.println(message);
                outStream.println(message);
            } else {
                readyHandlers.add(this);
            }
        }


        private void messageForClient(GameRoom room) {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                public void run() {
                    String message;
                    if (!clientSocket.isClosed() && user.getStatus().equals("INGAME") && room != null) {
                        if (room.getUser1().equals(user)) {
                            for (int i = 0; i < room.getShellList1().size(); ++i) {
                                Shell shell = room.getShellList1().get(i);
                                shell.setY(shell.getY() + 5);
                                if (shell.getY() > 265 && shell.getY() < 315 && shell.getX() - room.getX2() > -25 && shell.getX() - room.getX2() < 26) {
                                    room.getShellList1().remove(shell);
                                    room.setHits1(room.getHits1() + 1);
                                    room.setHealth2(room.getHealth2() - 5);
                                    if (room.getHealth2() < 0) {
                                        user.setHits(user.getHits() + room.getHits1());
                                        user.setMisses(user.getMisses() + room.getMisses1());
                                        enemy.setHits(enemy.getHits() + room.getHits2());
                                        enemy.setMisses(enemy.getMisses() + room.getMisses2());
                                        user.setStatus("ONLINE");
                                        enemy.setStatus("ONLINE");
                                        usersService.updateUser(user);
                                        usersService.updateUser(enemy);
                                        JSONObject json = new JSONObject();
                                        json.put("stage", "newGame");
                                        json.put("isWin", "false");
                                        int[] array = {room.getHits2(), room.getMisses2(),
                                                room.getUser2().getHits(), room.getUser2().getMisses()};
                                        json.put("stats", array);
                                        message = json.toString().replaceAll("\"", "\\\"");
                                        for (ClientHandler handler : clientHandlers) {
                                            if (handler.user.equals(enemy)) {
                                                handler.outStream.println(message);
                                            }
                                        }
                                        json.put("isWin", "true");
                                        array = new int[]{room.getHits1(), room.getMisses1(),
                                                room.getUser1().getHits(), room.getUser1().getMisses()};
                                        json.put("stats", array);
                                        message = json.toString().replaceAll("\"", "\\\"");
                                        outStream.println(message);
                                        timer.cancel();
                                        timer.purge();
                                        return;
                                    }
                                } else if (shell.getY() > 345) {
                                    room.setMisses1(room.getMisses1() + 1);
                                    room.getShellList1().remove(shell);
                                }
                            }
                            for (int i = 0; i < room.getShellList2().size(); ++i) {
                                Shell shell = room.getShellList2().get(i);
                                shell.setY(shell.getY() + 5);
                                if (shell.getY() > 265 && shell.getY() < 315 && shell.getX() - room.getX1() > -25 && shell.getX() - room.getX1() < 26) {
                                    room.getShellList2().remove(shell);
                                    room.setHits2(room.getHits2() + 1);
                                    room.setHealth1(room.getHealth1() - 5);
                                    if (room.getHealth1() < 0) {
                                        user.setHits(user.getHits() + room.getHits1());
                                        user.setMisses(user.getMisses() + room.getMisses1());
                                        enemy.setHits(enemy.getHits() + room.getHits2());
                                        enemy.setMisses(enemy.getMisses() + room.getMisses2());
                                        user.setStatus("ONLINE");
                                        enemy.setStatus("ONLINE");
                                        usersService.updateUser(user);
                                        usersService.updateUser(enemy);
                                        JSONObject json = new JSONObject();
                                        json.put("stage", "newGame");
                                        json.put("isWin", "true");
                                        int[] array = {room.getHits2(), room.getMisses2(), enemy.getHits(),
                                                enemy.getMisses()};
                                        json.put("stats", array);
                                        message = json.toString().replaceAll("\"", "\\\"");
                                        for (ClientHandler handler : clientHandlers) {
                                            if (handler.user.equals(enemy)) {
                                                handler.outStream.println(message);
                                                break;
                                            }
                                        }
                                        json.put("isWin", "false");
                                        array = new int[]{room.getHits1(), room.getMisses1(), user.getHits(),
                                                user.getMisses()};
                                        json.put("stats", array);
                                        message = json.toString().replaceAll("\"", "\\\"");
                                        outStream.println(message);
                                        timer.cancel();
                                        timer.purge();
                                        return;
                                    }
                                } else if (shell.getY() > 345) {
                                    room.setMisses2(room.getMisses2() + 1);
                                    room.getShellList2().remove(shell);
                                }
                            }
                        }
                        List<Shell> list1 = new ArrayList<>(room.getShellList1());
                        list1.removeIf(Objects::isNull);
                        List<Shell> list2 = new ArrayList<>(room.getShellList2());
                        list2.removeIf(Objects::isNull);
                        JSONObject json = new JSONObject();
                        json.put("stage", "inGame");
                        if (user.equals(room.getUser1())) {
                            json.put("playerX", room.getX1());
                            json.put("playerHealth", room.getHealth1());
                            json.put("enemyX", room.getX2());
                            json.put("enemyHealth", room.getHealth2());
                            json.put("enemyName", room.getUser2().getName());
                            json.put("playerShells", list1);
                            json.put("enemyShells", list2);
                        } else if (user.equals(room.getUser2())) {
                            json.put("playerX", room.getX2());
                            json.put("playerHealth", room.getHealth2());
                            json.put("enemyX", room.getX1());
                            json.put("enemyHealth", room.getHealth1());
                            json.put("enemyName", room.getUser1().getName());
                            json.put("playerShells", list2);
                            json.put("enemyShells", list1);
                        }
                        message = json.toString().replaceAll("\"", "\\\"");
                        outStream.println(message);
                    } else {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0L, 50);
        }
    }
}
