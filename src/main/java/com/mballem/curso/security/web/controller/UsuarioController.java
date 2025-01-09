package com.mballem.curso.security.web.controller;

import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.PerfilTipo;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("u")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    //abrir cadastro de usuarios (medico/admin/paciente)
    @GetMapping("/novo/cadastro/usuario")
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario){
        return "usuario/cadastro";
    }

    //abrir pagina de listagem
    @GetMapping("/lista")
    public String listarUsuarios(){
        return "usuario/lista";
    }

    // listar usuarios na datatables
    @GetMapping("/datatables/server/usuarios")
    public ResponseEntity<?> listarUsuariosDatatables(HttpServletRequest request){

        return ResponseEntity.ok(service.buscarTodos(request));
    }

    // salvar cadastro de usuario por administrador
    @PostMapping("/cadastro/salvar")
    public String SalvarUsuarios(Usuario usuario, RedirectAttributes attr){
        List<Perfil> perfis = usuario.getPerfis();
        if(perfis.size() > 2 ||
                perfis.containsAll(Arrays.asList(new Perfil(1L), new Perfil(3L))) ||
                perfis.containsAll(Arrays.asList(new Perfil(2L), new Perfil(3L)))){
            attr.addFlashAttribute("falha", "Paciente não pode ser Admin e/ou Médico.");
            attr.addFlashAttribute("usuario", usuario);
        }else {
            try {
                service.salvarUsuario(usuario);
                attr.addFlashAttribute("sucesso", "Operação realizada com sucesso!");
            } catch(DataIntegrityViolationException ex) {
                attr.addFlashAttribute("falha", "Usuário já cadastrado no sistema, Email ja existente!");
            }

        }
        return "redirect:/u/novo/cadastro/usuario";
    }

    // pre edição de credenciais de usuarios
    @GetMapping("/editar/credenciais/usuario/{id}")
    public ModelAndView preEditarCredenciais(@PathVariable("id") Long id){

        return new ModelAndView("usuario/cadastro","usuario", service.buscarPorId(id));
    }

    // pre edição de cadastro de usuarios
    @GetMapping("/editar/dados/usuario/{id}/perfis/{perfis}")
    public ModelAndView preEditarCadastroDadosPessoais(@PathVariable("id") Long usuarioId,
                                                       @PathVariable("perfis") Long[] perfisId){

        Usuario us = service.buscarPorIdePerfis(usuarioId, perfisId);

        if(us.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod())) &&
           !us.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod())) ){

           return new ModelAndView("usuario/cadastro", "usuario", us);
        } else if (us.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {

            return new ModelAndView("especialidade/especialidade");
        } else if (us.getPerfis().contains(new Perfil(PerfilTipo.PACIENTE.getCod()))) {
            ModelAndView model = new ModelAndView("error");
            model.addObject("status", 403);
            model.addObject("error", "Área Restrita");
            model.addObject("message", "Os dados do paciente são restritos a ele.");
            return model;
        }

        return new ModelAndView("redirect:/u/lista");
    }

}
