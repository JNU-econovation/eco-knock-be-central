package jnu.econovation.ecoknockbecentral.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupApplicationRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupApplicationResponse;
import jnu.econovation.ecoknockbecentral.group.service.GroupApplicationService;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GroupApplicationControllerTest {

    @Test
    void delegatesCreateAndReturnsEmptySuccess() {
        GroupApplicationService service = mock(GroupApplicationService.class);
        GroupApplicationController controller = new GroupApplicationController(service);
        EcoKnockUserDetails user = user(7L);

        var response = controller.create(
                3L,
                user,
                new CreateGroupApplicationRequest("지원")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().result()).isNull();
        verify(service).create(3L, 7L, "지원");
    }

    @Test
    void delegatesPendingListWithAuthenticatedMember() {
        GroupApplicationService service = mock(GroupApplicationService.class);
        GroupApplicationController controller = new GroupApplicationController(service);
        EcoKnockUserDetails user = user(7L);
        when(service.getPendingApplications(3L, 7L)).thenReturn(List.of());

        var response = controller.getPendingApplications(3L, user);

        assertThat(response.getBody().result()).isEmpty();
        verify(service).getPendingApplications(3L, 7L);
    }

    @Test
    void delegatesDetailAndProcessingEndpoints() {
        GroupApplicationService service = mock(GroupApplicationService.class);
        GroupApplicationController controller = new GroupApplicationController(service);
        EcoKnockUserDetails user = user(7L);
        GroupApplicationResponse application =
                new GroupApplicationResponse(9L, 8L, "지원자", "지원", null);
        when(service.getPendingApplication(3L, 9L, 7L)).thenReturn(application);

        assertThat(controller.getPendingApplication(3L, 9L, user).getBody().result())
                .isEqualTo(application);
        assertThat(controller.accept(3L, 9L, user).getBody().isSuccess()).isTrue();
        assertThat(controller.reject(3L, 9L, user).getBody().isSuccess()).isTrue();
        verify(service).getPendingApplication(3L, 9L, 7L);
        verify(service).accept(3L, 9L, 7L);
        verify(service).reject(3L, 9L, 7L);
    }

    private EcoKnockUserDetails user(Long id) {
        return new EcoKnockUserDetails(new MemberInfoDTO(
                id, id * 100, Role.USER, null, "회원", null, null
        ));
    }
}
