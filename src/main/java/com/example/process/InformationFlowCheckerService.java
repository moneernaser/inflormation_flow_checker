package com.example.process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InformationFlowCheckerService {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(InformationFlowCheckerService.class);


    @Autowired
    private HttpServletRequest request;

//    @RequestMapping(method = RequestMethod.POST, value = "/upload")
//    public String processFile(@RequestBody byte[] file) {
//        System.out.println(file.length);
//        //  System.out.println(new String(file));
//        return "hello world";
//    }

    @RequestMapping(value = "/upload2", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file,
                                           RedirectAttributes redirectAttributes, HttpSession httpSession) throws IOException {

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

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String ,Object> map = new HashMap<>();
            map.put("variablesList", variableList);
//            String json = gson.toJson(map);

            return ResponseEntity.ok(map);



        }
            return null;
        }


//    @RequestMapping(value = "/variables/all", method = RequestMethod.GET)
//    public String showGuestList(Model model, HttpSession httpSession) {
//        model.addAttribute("variablesList", httpSession.getAttribute("variablesList"));
//
//        return "results";
//    }

    @RequestMapping(value = "/variables/all2", method = RequestMethod.GET)
    public ModelAndView getVariables(Model model, HttpSession httpSession) {
        ModelAndView modelAndView = new ModelAndView("results");
        modelAndView.addObject("variablesList", httpSession.getAttribute("variablesList"));
        return modelAndView;
    }





    @RequestMapping(value = "/variables_levels", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> handleFileUpload( @RequestBody VariablesContainer container,
                                              RedirectAttributes redirectAttributes, HttpSession httpSession) throws IOException {

            System.out.println("hello");
            CompilationData compilationData = (CompilationData) httpSession.getAttribute("compilationData");
            compilationData.getAll();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String ,Object> map = new HashMap<>();

            return ResponseEntity.ok(map);

    }
}


