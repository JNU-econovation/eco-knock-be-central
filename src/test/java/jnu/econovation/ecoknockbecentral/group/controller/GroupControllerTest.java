package jnu.econovation.ecoknockbecentral.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.ChangeGroupLeaderRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupDetailRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupNameRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupRecruitmentRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
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
        EcoKnockUserDetails user = user(7L);
        when(service.browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC), 7L))
                .thenReturn(List.of());

        var response = controller.browse(false, GroupSort.NAME_ASC, user);

        assertThat(response.getBody().result()).isEmpty();
        verify(service).browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC), 7L);
    }

    @Test
    void delegatesManagementCommandsAndWrapsEmptySuccess() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);
        EcoKnockUserDetails user = user(7L);
        var name = new UpdateGroupNameRequest("새 이름");
        var detail = new UpdateGroupDetailRequest(GroupType.STUDY, "소개", 5);
        var recruitment = new UpdateGroupRecruitmentRequest(
                RecruitmentMode.ALWAYS,
                null,
                null
        );

        assertThat(controller.updateName(3L, user, name).getBody().result()).isNull();
        assertThat(controller.updateDetails(3L, user, detail).getBody().result()).isNull();
        assertThat(controller.updateRecruitment(3L, user, recruitment).getBody().result()).isNull();
        assertThat(controller.removeMember(3L, 8L, user).getBody().result()).isNull();
        assertThat(controller.changeLeader(
                3L,
                user,
                new ChangeGroupLeaderRequest(8L)
        ).getBody().result()).isNull();
        assertThat(controller.delete(3L, user).getBody().result()).isNull();

        verify(service).updateName(3L, 7L, name);
        verify(service).updateDetails(3L, 7L, detail);
        verify(service).updateRecruitment(3L, 7L, recruitment);
        verify(service).removeMember(3L, 7L, 8L);
        verify(service).changeLeader(3L, 7L, 8L);
        verify(service).delete(3L, 7L);
    }

    @Test
    void delegatesManagementMemberQuery() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);
        EcoKnockUserDetails user = user(7L);
        when(service.getMembersForManagement(3L, 7L)).thenReturn(List.of());

        var response = controller.getMembersForManagement(3L, user);

        assertThat(response.getBody().result()).isEmpty();
        verify(service).getMembersForManagement(3L, 7L);
    }

    @Test
    void delegatesMemberIdentityQueryWithAuthenticatedRole() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);
        EcoKnockUserDetails user = user(7L);
        when(service.getMembers(3L, 7L, Role.USER)).thenReturn(List.of());

        var response = controller.getMembers(3L, user);

        assertThat(response.getBody().result()).isEmpty();
        verify(service).getMembers(3L, 7L, Role.USER);
    }

    private EcoKnockUserDetails user(Long id) {
        return new EcoKnockUserDetails(new MemberInfoDTO(
                id, id * 100, Role.USER, null, "회원", null, null
        ));
    }
}
