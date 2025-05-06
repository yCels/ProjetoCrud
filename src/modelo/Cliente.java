/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import java.time.LocalDate;

/**
 *
 * @author CELSO
 */
public class Cliente {
    int codigo;
    String nome;
    String cpf;
    String email;
    String telefone;
    String endereço;
    LocalDate data;
    
    
    
    
    public int getCodigo(){
        return codigo;
    }
    
    public String getNome(){
        return nome;
    }
    
    public String getCpf(){
        return cpf;
    }
    
    public String getEmail(){
        return email;
    }
    
    public String getTelefone(){
        return telefone;
    }
    
    public String getEndereço(){
        return endereço;
    }
    
    public LocalDate getData(){
        return data;
    }
    
    public void setCodigo(int codigo){
        this.codigo = codigo;
    }
    
    public void setNome(String nome){
        this.nome = nome;
    }
    
    public void setCpf(String cpf){
        this.cpf = cpf;
        
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public void setTelefone(String telefone){
        this.telefone = telefone;
        
    }
    
    public void setEndereço(String endereço){
        this.endereço = endereço;
    }
    
    public void setData(LocalDate data){
        this.data = data;
    }
    
    
}   
