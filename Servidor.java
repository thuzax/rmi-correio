import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

public class Servidor implements Correio {
    private String endereco;
    private ArrayList<Usuario> usuarios;
    // private int porta;

    public Servidor() {
        this.endereco = "127.0.0.1";
        this.usuarios = new ArrayList<Usuario>();
        // this.porta = porta;
    }

    private boolean autenticar(String nome, String senha) {
        Usuario usuario = this.getUsuarioPorNome(nome);
        if(usuario == null) {
            return false;
        }
        if(usuario.getSenha().equals(senha)) {
            return true;
        }
        return false;
    }

    public Usuario getUsuarioPorNome(String nome) {
        for (Usuario usuario : this.usuarios){
            if(usuario.getUserName().equals(nome)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean jaExiste(String nome) {
        if(getUsuarioPorNome(nome) != null) {
            return true;
        }
        return false;
    }

    public boolean cadastrarUsuario (Usuario u) {
        if (this.jaExiste(u.getUserName())){
            return false;
        }
        this.usuarios.add(u);
        return true;
    }

    // Recupera a primeira mensagem na lista de mensagens do usuario; a mensagem deve ser removida
    // Exigir autenticação do usuário
    public Mensagem getMensagem (String userName, String senha) {
        boolean response = this.autenticar(userName,senha);
        if (response){
            Usuario usuario = this.getUsuarioPorNome(userName);
            Mensagem mensagem = usuario.getPrimeiraMensagem();
            return mensagem;
        }
        return null;
    }

    // retorna o número de mensagens na fila de mensagens dos usuário
    // Exigir autenticação do usuário
    public int getNMensagens (String userName, String senha) {
        if(!this.autenticar(userName, senha)) {
            return -1;
        }
        Usuario usuario = this.getUsuarioPorNome(userName);
        return usuario.getQuantidadeMensagens();
    }

    // Exigir autenticação do usuário (senha do remetente, incluído como atributo da mensagem)
    public boolean sendMensagem (Mensagem m, String senha, String userNameDestinatario) {
        System.out.println(m.getUserNameRemetente() + " " + userNameDestinatario);
        if(!this.autenticar(m.getUserNameRemetente(), senha)) {
            return false;
        }
        Usuario destino = this.getUsuarioPorNome(userNameDestinatario);
        if(destino == null) {
            return false;
        }
        destino.adicionaMensagem(m);
        return true;
    }

    public static void main(String args[]) {
        try {
            Servidor servidor = new Servidor();
            System.setProperty("java.rmi.server.hostname", servidor.endereco);

            //Create and export a remote object
            Correio stub = (Correio) UnicastRemoteObject.exportObject(servidor,0);
            
            //Register the remote object with a Java RMI registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Correio", stub);

            System.out.println("Server Ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}