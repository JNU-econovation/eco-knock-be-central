package jnu.econovation.ecoknockbecentral.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.service.GroupService;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GroupControllerTest {

    @Test
    void wrapsCreateResponseAndAuthenticatedMemberId() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);
        EcoKnockUserDetails user = user(7L);
        var request = mock(jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest.class);
        when(service.create(7L, request)).thenReturn(new CreateGroupResponse(11L));

        var response = controller.create(user, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().result()).isEqualTo(new CreateGroupResponse(11L));
        verify(service).create(7L, request);
    }

    @Test
    void appliesBrowseDefaultsPassedByWebBinding() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);
        when(service.browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC)))
                .thenReturn(List.of());

        var response = controller.browse(false, GroupSort.NAME_ASC);

        assertThat(response.getBody().result()).isEmpty();
        verify(service).browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC));
    }

    private EcoKnockUserDetails user(Long id) {
        return new EcoKnockUserDetails(new MemberInfoDTO(
                id, id * 100, Role.USER, null, "회원", null, null
        ));
    }
}
