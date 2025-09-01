package kr.hhplus.be.commerce;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kr.hhplus.be.commerce.global.response.EmptyResponse;

@RestController
public class IndexController {

	@GetMapping("/")
	@ResponseStatus(HttpStatus.OK)
	public EmptyResponse index() {
		return EmptyResponse.INSTANCE;
	}
}
