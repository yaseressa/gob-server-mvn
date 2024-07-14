package com.kq.gobweb;


import com.kq.gob.Gob.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gob")
public class GobWebController {
    @CrossOrigin
    @PostMapping
    public ResponseEntity<String> runGob(@RequestBody GobRequest code) {
        String res = Gob.run(code.code);
        System.out.println(res);
        return new ResponseEntity<>(res, HttpStatus.OK);

    }
}

