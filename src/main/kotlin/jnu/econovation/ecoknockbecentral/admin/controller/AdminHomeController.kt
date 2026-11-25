package jnu.econovation.ecoknockbecentral.admin.controller

import io.swagger.v3.oas.annotations.Hidden
import jnu.econovation.ecoknockbecentral.admin.config.GrafanaConfig
import jnu.econovation.ecoknockbecentral.admin.constant.AdminConstant.ADMIN_HOME_PATH
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Hidden
class AdminHomeController(
    private val grafanaConfig: GrafanaConfig,
) {
    @GetMapping("/")
    fun root(): String {
        return "redirect:$ADMIN_HOME_PATH"
    }

    @GetMapping("/admin", "/admin/")
    fun home(model: Model): String {
        model.addAttribute("grafanaUrl", grafanaConfig.url)
        return "admin/index"
    }
}
