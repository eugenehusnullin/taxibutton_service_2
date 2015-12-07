package tb.admin;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tb.dao.ITariffDefinitionMapAreaDao;
import tb.domain.TariffDefinitionMapArea;

@RequestMapping("/tariffdefmaparea")
@Controller("adminTariffDefinitionMapAreaController")
public class TariffDefinitionMapAreaController {
	private static final Logger logger = LoggerFactory.getLogger(TariffDefinitionMapAreaController.class);

	@Autowired
	private ITariffDefinitionMapAreaDao tariffDefinitionMapAreaDao;

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String add() {
		return "tariffdefmaparea/add";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String add(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");
		String body = request.getParameter("body");

		// validate json in body field
		JSONTokener tokener = new JSONTokener(body);
		if (tokener.nextValue() instanceof JSONObject && tokener.more() == false) {
			TariffDefinitionMapArea tariffDefinitionMapArea = new TariffDefinitionMapArea();
			tariffDefinitionMapArea.setName(name);
			tariffDefinitionMapArea.setBody(body);
			tariffDefinitionMapArea.setCreatedDate(new Date());
			tariffDefinitionMapAreaDao.add(tariffDefinitionMapArea);
		} else {
			model.addAttribute("result", "JSON is not valid.");
			return "result";
		}

		return "redirect:list";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String edit(@RequestParam("name") String name, Model model) {
		TariffDefinitionMapArea tariffDefinitionMapArea = tariffDefinitionMapAreaDao.get(name);
		if (tariffDefinitionMapArea == null) {
			logger.warn("TariffDefinitionMapArea not found, name=" + name);
			return "redirect:list";
		}
		model.addAttribute("name", name);
		model.addAttribute("body", tariffDefinitionMapArea.getBody());
		return "tariffdefmaparea/edit";
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");
		String newName = request.getParameter("newname");
		String body = request.getParameter("body");

		// validate json in body field
		JSONTokener tokener = new JSONTokener(body);
		if (tokener.nextValue() instanceof JSONObject && tokener.more() == false) {
			TariffDefinitionMapArea tariffDefinitionMapArea = tariffDefinitionMapAreaDao.get(name);
			tariffDefinitionMapArea.setName(newName);
			tariffDefinitionMapArea.setBody(body);
			tariffDefinitionMapArea.setEditedDate(new Date());
			tariffDefinitionMapAreaDao.update(tariffDefinitionMapArea);
		} else {
			model.addAttribute("result", "JSON is not valid.");
			return "result";
		}

		return "redirect:list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model) {
		List<TariffDefinitionMapArea> list = tariffDefinitionMapAreaDao.getAll();
		model.addAttribute("tariffdefmapareas", list);
		return "tariffdefmaparea/list";
	}

	@RequestMapping(value = "/del", method = RequestMethod.GET)
	public String del(@RequestParam("name") String name) {
		tariffDefinitionMapAreaDao.delete(name);

		return "redirect:list";
	}
}
