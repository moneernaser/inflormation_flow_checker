package com.example.process;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
public class InformationFlowCheckerService {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(InformationFlowCheckerService.class);


    @Autowired
    private HttpServletRequest request;


    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, HttpSession httpSession) throws IOException {

        if (!file.isEmpty()) {
            String uploadsDir = "/uploads/";
            String realPathtoUploads = request.getServletContext().getRealPath(uploadsDir);
            if (!new File(realPathtoUploads).exists()) {
                new File(realPathtoUploads).mkdir();
            }

            log.info("realPathtoUploads = {}", realPathtoUploads);


            String orgName = file.getOriginalFilename();
            String filePath = realPathtoUploads + orgName;
            File dest = new File(filePath);
            file.transferTo(dest);
            CompilationData compilationData = new CompilationData(filePath);
            httpSession.setAttribute("compilationData", compilationData);
            List<Variable> variableList = compilationData.getAll();
            httpSession.setAttribute("variablesList", variableList);
            Map<String ,Object> map = new HashMap<>();
            map.put("variablesList", variableList);
            return ResponseEntity.ok(map);

        }
            return null;
        }



    @RequestMapping(value = "/variables/all", method = RequestMethod.GET)
    public ModelAndView getVariables(Model model, HttpSession httpSession) {
        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("variablesList", httpSession.getAttribute("variablesList"));
        return modelAndView;
    }





    @RequestMapping(value = "/variables_levels", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> checkAssignmentLegality( @RequestBody VariablesContainer container, HttpSession httpSession) throws IOException {

        Map<Variable, Set<Variable>> illegalFlows = new HashMap<>();
        CompilationData compilationData = (CompilationData) httpSession.getAttribute("compilationData");
        setSecurityLevels(compilationData.getAll(), container);
        Map<Variable, Set<Variable>> dependencies = compilationData.buildGraphDependencies();
        setSecurityLevelsForDependencies(dependencies, container);
        if (container != null) {
            for (Variable v : container.getVars()) {
                Set<Variable> dependent = dependencies.get(v);
                if (dependent != null && !dependent.isEmpty()) {
                    int vSecurityLevel = SecurityLevel.fromString(v.getSecurityLevel());
                    for (Variable d : dependent) {
                        int dSecurityLevel =  getSecurityLevelOf(d, container);
                        if (dSecurityLevel > vSecurityLevel) {
                            addIllegalFlow(illegalFlows, v, d);
                        }
                    }
                }
            }
        }
            List<IllegalMapping> illegalMappings = toList(illegalFlows);
            return ResponseEntity.ok(illegalMappings);

    }

    private List<IllegalMapping> toList(Map<Variable, Set<Variable>> illegalFlows) {
        List<IllegalMapping> illegalMappings = new ArrayList<>();
        for(Map.Entry<Variable, Set<Variable>> entry: illegalFlows.entrySet()) {
            illegalMappings.add(new IllegalMapping(entry.getKey(), entry.getValue()));
        }
        return illegalMappings;
    }

    private int getSecurityLevelOf(Variable d, VariablesContainer container) {
        for (Variable v : container.getVars()) {
            if (v.equals(d))
                return SecurityLevel.fromString(v.getSecurityLevel()); ;
        }
        throw new IllegalStateException("variable not submitted!");
    }

    private void addIllegalFlow(Map<Variable, Set<Variable>> illegalFlows, Variable v, Variable d) {
        Set<Variable> dependent = illegalFlows.get(v);
        if (dependent != null) {
            dependent.add(d);
        } else {
            dependent = new LinkedHashSet<>();
            dependent.add(d);
            illegalFlows.put(v, dependent);
        }
    }


    @RequestMapping(value = "/example/BankAccount", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> handleFileUpload(HttpSession httpSession) throws IOException {


            ClassLoader classLoader = getClass().getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream("public/BankAccount.java");
            CompilationData compilationData = new CompilationData(resourceAsStream);
            httpSession.setAttribute("compilationData", compilationData);
            List<Variable> variableList = compilationData.getAll();
            httpSession.setAttribute("variablesList", variableList);
            Map<String ,Object> map = new HashMap<>();
            map.put("variablesList", variableList);
            return ResponseEntity.ok(map);

        }

    public void setSecurityLevels(List<Variable> all, VariablesContainer container) {
        for (Variable v : all) {
            for (Variable userVariable : container.getVars()) {
                if (v.equals(userVariable)) {
                    v.setSecurityLevel(userVariable.getSecurityLevel());
                }
            }
        }
    }

    public void setSecurityLevelsForDependencies(Map<Variable, Set<Variable>> dependencies, VariablesContainer container) {
        for (Variable v : dependencies.keySet()) {
            List<Variable> vs = new ArrayList<>();
            vs.add(v);
            vs.addAll(dependencies.get(v));
            setSecurityLevels(vs, container);
        }
    }
}


