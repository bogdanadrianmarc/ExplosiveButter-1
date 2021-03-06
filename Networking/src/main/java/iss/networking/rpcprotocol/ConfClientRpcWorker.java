package iss.networking.rpcprotocol;

import iss.model.*;
//import iss.networking.dto.*;
import iss.networking.dto.*;
import iss.networking.dto.File_DTO;
import iss.services.ConfException;
import iss.services.IConfClient;
import iss.services.IConfServer;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by Bitten Apple on 15-May-17.
 */
public class ConfClientRpcWorker implements Runnable, IConfClient {

    //referinta la server + conexiunea
    private IConfServer server;
    private Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    public ConfClientRpcWorker(IConfServer server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (connected) {
            try {
                Object request = input.readObject();
                Response response = handleRequest((Request) request);
                if (response != null) {
                    sendResponse(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
    }

//    @Override
//    public void conferencesUpdate(List<Conference> conferenceList) throws ConfException {
//        Conference[] conferences = new Conference[conferenceList.size()];
//        for (int i = 0; i < conferenceList.size(); i++) {
//            conferences[i] = conferenceList.get(i);
//        }
//        ConferenceDTO[] conferenceDTOS = DTOUtils.getDTO(conferences);
//        Response resp = new Response.Builder().type(ResponseType.CONFERENCES_UPDATE).data(conferenceDTOS).build();
//        System.out.println("Conferences received");
//        try {
//            sendResponse(resp);
//        } catch (IOException e) {
//            throw new ConfException("Sending error: " + e);
//        }
//    }

    private static Response okResponse = new Response.Builder().type(ResponseType.OK).build();

    //  private static Response errorResponse=new Response.Builder().type(ResponseType.ERROR).build();
    private Response handleRequest(Request request) {
        Response response = null;

        //daca request-ul e de login
        if (request.type() == RequestType.LOGIN) {
            System.out.println("Login request ..." + request.type());
//            UserDTO userDTO = (UserDTO) request.data();
//            User user = DTOUtils.getFromDTO(userDTO);
            User user = (User) request.data();
            try {
                User u = server.login(user, this);
                return new Response.Builder().type(ResponseType.OK).data(u).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de logout
        if (request.type() == RequestType.LOGOUT) {
            System.out.println("Logout request");
            // LogoutRequest logReq=(LogoutRequest)request;
//            UserDTO userDTO = (UserDTO) request.data();
//            User user = DTOUtils.getFromDTO(userDTO);
            User user = (User) request.data();
            try {
                server.logout(user, this);
                connected = false;
                return okResponse;

            } catch (ConfException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de la register
        if (request.type() == RequestType.REGISTER){
            System.out.println("Register request");
//            UserDTO userDTO = (UserDTO) request.data();
//            User user = DTOUtils.getFromDTO(userDTO);
            User user = (User) request.data();
            try{
                server.register(user);
                connected=false;
                return okResponse;
            } catch (ConfException e) {
//                e.printStackTrace();
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de conferences
        if (request.type() == RequestType.CONFERENCES) {
            System.out.println("Conferences request ..." + request.type());
            try {
                Conference[] conferences = server.getAllConferences();
//                ConferenceDTO[] conferenceDTOS = DTOUtils.getDTO(conferences);
//                return new Response.Builder().type(ResponseType.OK).data(conferenceDTOS).build();
                return new Response.Builder().type(ResponseType.OK).data(conferences).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de sessions for conference
        if (request.type() == RequestType.SESSIONS_FOR_CONFERENCE) {
            System.out.println("Sessions for conference request ..." + request.type());
//            ConferenceDTO conferenceDTO = (ConferenceDTO) request.data();
//            Conference conference = DTOUtils.getFromDTO(conferenceDTO);
            Conference conference = (Conference) request.data();
            try {
                Session[] sessions = server.getSessions(conference);
//                SessionDTO[] sessionDTOS = DTOUtils.getDTO(sessions);
//                return new Response.Builder().type(ResponseType.OK).data(sessionDTOS).build();
                return new Response.Builder().type(ResponseType.OK).data(sessions).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de roles for user
        if (request.type() == RequestType.ROLES_FOR_USER) {
            System.out.println("Roles for user request ..." + request.type());
//            UserDTO userDTO = (UserDTO) request.data();
//            User user = DTOUtils.getFromDTO(userDTO);
            User user = (User) request.data();
            try {
                Role[] roles = server.getRoles(user);
//                RoleDTO[] roleDTOS = DTOUtils.getDTO(roles);
//                return new Response.Builder().type(ResponseType.OK).data(roleDTOS).build();
                return new Response.Builder().type(ResponseType.OK).data(roles).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de conferences for user and role
        if (request.type() == RequestType.CONFERENCES_FOR_USER_AND_ROLE) {
            System.out.println("Conferences for user and role request ..." + request.type());
//            UserRoleDTO userRoleDTO = (UserRoleDTO) request.data();
//            UserRole userRole = DTOUtils.getFromDTO(userRoleDTO);
            UserRole userRole = (UserRole) request.data();
            User user = userRole.getUser();
            Role role = userRole.getRole();
            try {
                Conference[] conferences = server.getConferences(user, role);
//                ConferenceDTO[] conferenceDTOS = DTOUtils.getDTO(conferences);
//                return new Response.Builder().type(ResponseType.OK).data(conferenceDTOS).build();
                return new Response.Builder().type(ResponseType.OK).data(conferences).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de conferences with deadline not expired
        if (request.type() == RequestType.CONFERENCES_WITH_DEADLINE_NOT_EXPIRED) {
            System.out.println("Conferences with deadline not expired request ..." + request.type());
            try {
                Conference[] conferences = server.getAllConferencesDeadline();
//                ConferenceDTO[] conferenceDTOS = DTOUtils.getDTO(conferences);
//                return new Response.Builder().type(ResponseType.OK).data(conferenceDTOS).build();
                return new Response.Builder().type(ResponseType.OK).data(conferences).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de abstract
        if (request.type() == RequestType.ABSTRACT) {
            System.out.println("Abstract request ..." + request.type());
            Abstract_Details abstract_details = (Abstract_Details) request.data();
            try {

                File_DTO file_dto = new File_DTO();

                File file = new File(abstract_details.getFilePath());
                // Get the size of the file
                long length = file.length();
                byte[] bytes = new byte[16 * 1024];
                InputStream in = new FileInputStream(file);

                int count;
                while ((count = in.read(bytes)) > 0) {
//                    out.write(bytes, 0, count);
                    file_dto.put(bytes);
                }

                return new Response.Builder().type(ResponseType.OK).data(file_dto).build();
            } catch (FileNotFoundException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            } catch (IOException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de full
        if (request.type() == RequestType.FULL) {
            System.out.println("Full request ..." + request.type());
            Abstract_Details abstract_details = (Abstract_Details) request.data();
            try {
                File file = server.getFull(abstract_details);

                File_DTO file_dto = new File_DTO();

                String[] split = file.getPath().split("/");
                String filename = split[split.length-1];

                file_dto.setFilepath(filename);

                // Get the size of the file
                long length = file.length();
                byte[] bytes = new byte[16 * 1024];
                InputStream in = new FileInputStream(file);

                int count;
                while ((count = in.read(bytes)) > 0) {
//                    out.write(bytes, 0, count);
                    file_dto.put(bytes);
                }

                return new Response.Builder().type(ResponseType.OK).data(file_dto).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //daca request-ul e de name and topic
        if (request.type() == RequestType.NAME_AND_TOPIC) {
            System.out.println("Name and topic request ..." + request.type());
            Session session = (Session) request.data();
            try {
                Abstract_Details[] abstract_details = server.getNameAndTopic(session);
                return new Response.Builder().type(ResponseType.OK).data(abstract_details).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de review
        if (request.type() == RequestType.REVIEW) {
            System.out.println("Review request ..." + request.type());
            Review_DTOHelper review_dtoHelper = (Review_DTOHelper) request.data();
            Abstract_Details abstract_details = review_dtoHelper.getAbstract_details();
            String qualifier = review_dtoHelper.getQualifier();
            String recomandare = review_dtoHelper.getRecomandare();
            User user = review_dtoHelper.getUser();
            try {
                server.review(abstract_details, qualifier, recomandare, user);
                return new Response.Builder().type(ResponseType.OK).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de sesiuni conferinta user rol
        if (request.type() == RequestType.SESIUNI_CONFERINTA_USER_ROL) {
            System.out.println("Sesiuni conferinta user rol request ..." + request.type());
            Sesiuni_Conferinta_User_Rol_DTOHelper sesiuni_conferinta_user_rol_dtoHelper = (Sesiuni_Conferinta_User_Rol_DTOHelper) request.data();
            Conference conference = sesiuni_conferinta_user_rol_dtoHelper.getConference();
            User user = sesiuni_conferinta_user_rol_dtoHelper.getUser();
            Role role = sesiuni_conferinta_user_rol_dtoHelper.getRole();
            try {
                Session[] sessions = server.getSesiuniConferintaUserRol(conference, user, role);
                return new Response.Builder().type(ResponseType.OK).data(sessions).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de attend
        if (request.type() == RequestType.ATTEND) {
            System.out.println("Attend request ..." + request.type());
            Attend_DTOHelper attend_dtoHelper = (Attend_DTOHelper) request.data();
            User user = attend_dtoHelper.getUser();
            Role role = attend_dtoHelper.getRole();
            Conference conference = attend_dtoHelper.getConference();
            Session session = attend_dtoHelper.getSession();
            try {
                server.attend(user, role, conference, session);
                return new Response.Builder().type(ResponseType.OK).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de verifica
        if (request.type() == RequestType.VERIFICA) {
            System.out.println("Verifica request ..." + request.type());
            Verifica_DTOHelper verifica_dtoHelper = (Verifica_DTOHelper) request.data();
            User user = verifica_dtoHelper.getUser();
            Paper paper = verifica_dtoHelper.getPaper();
            try {
                boolean verif = server.verifica(user, paper);
                return new Response.Builder().type(ResponseType.OK).data(verif).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de submit abstract
        if (request.type() == RequestType.SUBMIT_ABSTRACT) {
            System.out.println("Submit abstract request ..." + request.type());
            Abstract_DTOHelper abstract_dtoHelper = (Abstract_DTOHelper) request.data();
            String name = abstract_dtoHelper.getName();
            String topics = abstract_dtoHelper.getTopics();
            String keywords = abstract_dtoHelper.getKeywords();
            String filepath = abstract_dtoHelper.getFilepath();
            String detalii_autor = abstract_dtoHelper.getDetalii_autor();
            Session session = abstract_dtoHelper.getSession();
            User user = abstract_dtoHelper.getUser();
            File_DTO file_dto = abstract_dtoHelper.getFile_dto();
            try {
                File file = server.submitAbstractFlorin(name, topics, keywords, filepath, detalii_autor, session, user);

                try {

                    if(file.exists() && !file.isDirectory()) {
                        // do something
                        if(file.delete()){
                            System.out.println(file.getName() + " is deleted!");
                        }else{
                            System.out.println("Delete operation is failed.");
                        }
                    }

                    String filename = file.getPath();
//                String abc = new String("abc");
                    OutputStream out = new FileOutputStream("./Server/files/"+filename);

                    Map<Integer, byte[]> map = file_dto.getDictionary();

                    int i;
                    for (i=0; i<map.keySet().toArray().length; i++) {
                        byte[] bytes = map.get(i);
                        out.write(bytes, 0, bytes.length);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new Response.Builder().type(ResponseType.OK).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        //daca request-ul e de submit full
        if (request.type() == RequestType.SUBMIT_FULL) {
            System.out.println("Submit full request ..." + request.type());
            Full_DTOHelper full_dtoHelper = (Full_DTOHelper) request.data();
            String filepath = full_dtoHelper.getFilepath();
            Session session = full_dtoHelper.getSession();
            User user = full_dtoHelper.getUser();
            File_DTO file_dto = full_dtoHelper.getFile_dto();
            try {
                File file = server.submitFullFlorin(filepath, session, user);

                try {

                    if(file.exists() && !file.isDirectory()) {
                        // do something
                        if(file.delete()){
                            System.out.println(file.getName() + " is deleted!");
                        }else{
                            System.out.println("Delete operation is failed.");
                        }
                    }

                    String filename = file.getPath();
//                String abc = new String("abc");
                    OutputStream out = new FileOutputStream("./Server/files/"+filename);

                    Map<Integer, byte[]> map = file_dto.getDictionary();

                    int i;
                    for (i=0; i<map.keySet().toArray().length; i++) {
                        byte[] bytes = map.get(i);
                        out.write(bytes, 0, bytes.length);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new Response.Builder().type(ResponseType.OK).build();
            } catch (ConfException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        return response;
    }

    private void sendResponse(Response response) throws IOException {
        System.out.println("sending response " + response);
        output.writeObject(response);
        output.flush();
    }

}
