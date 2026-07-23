package jnu.econovation.ecoknockbecentral.group.controller;

import static jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication;
import jnu.econovation.ecoknockbecentral.common.openapi.service.ApiDocAccessService;
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil;
import jnu.econovation.ecoknockbecentral.group.repository.GroupApplicationRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.web.client.RestClient;

@SpringBootTest(
        classes = EcoKnockBeCentralApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GroupAuthorizationE2ETest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupApplicationRepository groupApplicationRepository;
    private final ApiDocAccessService apiDocAccessService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient;
    private final List<Long> memberIds = new ArrayList<>();
    private final List<Long> groupIds = new ArrayList<>();

    GroupAuthorizationE2ETest(
            @LocalServerPort int port,
            MemberRepository memberRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupApplicationRepository groupApplicationRepository,
            ApiDocAccessService apiDocAccessService,
            JwtUtil jwtUtil,
            JdbcTemplate jdbcTemplate
    ) {
        this.memberRepository = memberRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupApplicationRepository = groupApplicationRepository;
        this.apiDocAccessService = apiDocAccessService;
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void tearDown() {
        apiDocAccessService.update(false);
        groupIds.forEach(id -> jdbcTemplate.update("DELETE FROM groups WHERE id = ?", id));
        memberIds.forEach(id -> jdbcTemplate.update("DELETE FROM member WHERE id = ?", id));
        groupIds.clear();
        memberIds.clear();
    }

    @Test
    void guestCanReadPublicGroupApisButCannotMutateOrReadProtectedData() throws Exception {
        AuthenticatedMember leader = createMember(false, "공개리더");
        long groupId = createGroup(leader, uniqueGroupName("공개"));
        AuthenticatedMember guest = createGuest();

        assertThat(exchange(HttpMethod.GET, "/groups", null, null).status())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        HttpResult browse = exchange(HttpMethod.GET, "/groups", guest.token(), null);
        HttpResult myGroups = exchange(HttpMethod.GET, "/groups/me", guest.token(), null);
        HttpResult detail = exchange(HttpMethod.GET, "/groups/" + groupId, guest.token(), null);
        HttpResult create = exchange(
                HttpMethod.POST,
                "/groups",
                guest.token(),
                createGroupBody(uniqueGroupName("게스트"))
        );
        HttpResult applications = exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/applications",
                guest.token(),
                null
        );
        HttpResult management = exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/members/management",
                guest.token(),
                null
        );

        assertThat(browse.status()).isEqualTo(HttpStatus.OK);
        assertThat(myGroups.status()).isEqualTo(HttpStatus.OK);
        assertThat(json(myGroups).path("result").isEmpty()).isTrue();
        assertThat(detail.status()).isEqualTo(HttpStatus.OK);
        assertThat(create.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertError(applications, HttpStatus.FORBIDDEN, "GROUP_403_001");
        assertError(management, HttpStatus.FORBIDDEN, "GROUP_403_002");
    }

    @Test
    void userCanCreateBrowseApplyAndLeaderCanProcessApplications() throws Exception {
        AuthenticatedMember leader = createMember(false, "흐름리더");
        AuthenticatedMember applicant = createMember(false, "흐름지원자");
        String groupName = uniqueGroupName("흐름");

        HttpResult create = exchange(
                HttpMethod.POST,
                "/groups",
                leader.token(),
                createGroupBody(groupName)
        );
        assertThat(create.status()).isEqualTo(HttpStatus.OK);
        long groupId = json(create).path("result").path("groupId").asLong();
        groupIds.add(groupId);

        assertThat(exchange(HttpMethod.GET, "/groups", applicant.token(), null).body())
                .contains(groupName);
        assertThat(exchange(HttpMethod.GET, "/groups/" + groupId, applicant.token(), null).status())
                .isEqualTo(HttpStatus.OK);
        assertError(
                exchange(
                        HttpMethod.POST,
                        "/groups",
                        leader.token(),
                        createGroupBody("")
                ),
                HttpStatus.BAD_REQUEST,
                "COMMON_400_001"
        );

        HttpResult application = exchange(
                HttpMethod.POST,
                "/groups/" + groupId + "/applications",
                applicant.token(),
                "{\"content\":\"첫 지원\"}"
        );
        assertThat(application.status()).isEqualTo(HttpStatus.OK);
        assertError(
                exchange(
                        HttpMethod.POST,
                        "/groups/" + groupId + "/applications",
                        applicant.token(),
                        "{\"content\":\"중복 지원\"}"
                ),
                HttpStatus.CONFLICT,
                "GROUP_409_003"
        );

        HttpResult pending = exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/applications",
                leader.token(),
                null
        );
        long applicationId = json(pending).path("result").get(0).path("applicationId").asLong();
        assertThat(exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/applications/" + applicationId,
                leader.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);
        assertThat(exchange(
                HttpMethod.PUT,
                "/groups/" + groupId + "/applications/" + applicationId + "/reject",
                leader.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);

        assertThat(exchange(
                HttpMethod.POST,
                "/groups/" + groupId + "/applications",
                applicant.token(),
                "{\"content\":\"재지원\"}"
        ).status()).isEqualTo(HttpStatus.OK);
        long reappliedId = json(exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/applications",
                leader.token(),
                null
        )).path("result").get(0).path("applicationId").asLong();
        assertThat(exchange(
                HttpMethod.PUT,
                "/groups/" + groupId + "/applications/" + reappliedId + "/accept",
                leader.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);

        assertError(
                exchange(
                        HttpMethod.PUT,
                        "/groups/" + groupId + "/name",
                        applicant.token(),
                        "{\"name\":\"권한없음\"}"
                ),
                HttpStatus.FORBIDDEN,
                "GROUP_403_002"
        );
        assertError(
                exchange(HttpMethod.GET, "/groups/" + Long.MAX_VALUE, leader.token(), null),
                HttpStatus.NOT_FOUND,
                "GROUP_404_001"
        );
        assertError(
                exchange(
                        HttpMethod.POST,
                        "/groups/" + groupId + "/applications",
                        applicant.token(),
                        "{\"content\":\"다시 지원\"}"
                ),
                HttpStatus.UNPROCESSABLE_CONTENT,
                "GROUP_422_005"
        );
    }

    @Test
    void nonMemberAdminCanManageTransferAndDeleteGroupWithCascade() throws Exception {
        AuthenticatedMember leader = createMember(false, "관리리더");
        AuthenticatedMember member = createMember(false, "관리멤버");
        AuthenticatedMember applicant = createMember(false, "삭제지원자");
        AuthenticatedMember admin = createMember(true, "외부관리자");
        long groupId = createGroup(leader, uniqueGroupName("관리"));

        apply(groupId, member, "가입 지원");
        long memberApplicationId = pendingApplicationId(groupId, leader);
        assertThat(exchange(
                HttpMethod.PUT,
                "/groups/" + groupId + "/applications/" + memberApplicationId + "/accept",
                leader.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);
        apply(groupId, applicant, "삭제될 지원");

        assertThat(exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/members/management",
                admin.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);
        assertThat(exchange(
                HttpMethod.PUT,
                "/groups/" + groupId + "/detail",
                admin.token(),
                "{\"type\":\"DEPARTMENT\",\"introduction\":\"관리 수정\",\"capacity\":5}"
        ).status()).isEqualTo(HttpStatus.OK);
        assertThat(exchange(
                HttpMethod.PUT,
                "/groups/" + groupId + "/leader",
                admin.token(),
                "{\"memberId\":" + member.id() + "}"
        ).status()).isEqualTo(HttpStatus.OK);
        assertThat(exchange(
                HttpMethod.DELETE,
                "/groups/" + groupId + "/members/" + leader.id(),
                admin.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);
        assertThat(exchange(
                HttpMethod.DELETE,
                "/groups/" + groupId,
                admin.token(),
                null
        ).status()).isEqualTo(HttpStatus.OK);

        assertThat(groupRepository.existsById(groupId)).isFalse();
        assertThat(groupMemberRepository.findAllByGroupId(groupId)).isEmpty();
        assertThat(groupApplicationRepository.findAll()).noneMatch(application ->
                application.getGroup().getId().equals(groupId));
        groupIds.remove(groupId);
    }

    @Test
    void generatedOpenApiMatchesGroupRuntimeContract() throws Exception {
        apiDocAccessService.update(true);

        HttpResult result = exchange(HttpMethod.GET, "/v3/api-docs", null, null);

        assertThat(result.status()).isEqualTo(HttpStatus.OK);
        JsonNode document = json(result);
        JsonNode paths = document.path("paths");
        assertThat(paths.path("/groups").has("get")).isTrue();
        assertThat(paths.path("/groups").has("post")).isTrue();
        assertThat(paths.path("/groups/{groupId}").has("get")).isTrue();
        assertThat(paths.path("/groups/{groupId}/applications/{applicationId}/accept").has("put"))
                .isTrue();
        assertThat(paths.has("/admin")).isFalse();
        assertThat(paths.has("/admin/")).isFalse();
        assertThat(paths.has("/admin/login")).isFalse();
        assertThat(paths.has("/admin/access-denied")).isFalse();

        JsonNode browseParameters = paths.path("/groups").path("get").path("parameters");
        assertThat(browseParameters).anySatisfy(parameter ->
                assertThat(parameter.path("name").asText()).isIn("excludeClosed", "sort"));
        assertThat(browseParameters.toString()).contains("excludeClosed", "sort");
        assertThat(paths.path("/groups").path("post").path("requestBody")
                .path("content").has(MediaType.APPLICATION_JSON_VALUE)).isTrue();
        assertThat(paths.path("/groups/{groupId}").path("get").path("responses")
                .path("200").path("content").has(MediaType.APPLICATION_JSON_VALUE)).isTrue();

        JsonNode examples = document.path("components").path("examples");
        assertThat(examples.has("GroupNotFound")).isTrue();
        assertThat(examples.has("GroupLeaderPermissionRequired")).isTrue();
        assertThat(examples.has("GroupApplicationAlreadyPending")).isTrue();
        assertThat(examples.has("GroupCapacityInvalid")).isTrue();
        assertThat(result.body()).doesNotContain("\"example\":null", "\"example\": null");
    }

    private long createGroup(AuthenticatedMember leader, String name) throws Exception {
        HttpResult response = exchange(
                HttpMethod.POST,
                "/groups",
                leader.token(),
                createGroupBody(name)
        );
        assertThat(response.status()).isEqualTo(HttpStatus.OK);
        long groupId = json(response).path("result").path("groupId").asLong();
        groupIds.add(groupId);
        return groupId;
    }

    private void apply(long groupId, AuthenticatedMember applicant, String content) {
        assertThat(exchange(
                HttpMethod.POST,
                "/groups/" + groupId + "/applications",
                applicant.token(),
                "{\"content\":\"" + content + "\"}"
        ).status()).isEqualTo(HttpStatus.OK);
    }

    private long pendingApplicationId(long groupId, AuthenticatedMember requester)
            throws Exception {
        return json(exchange(
                HttpMethod.GET,
                "/groups/" + groupId + "/applications",
                requester.token(),
                null
        )).path("result").get(0).path("applicationId").asLong();
    }

    private AuthenticatedMember createMember(boolean admin, String name) {
        long suffix = Math.abs(System.nanoTime());
        Member member = Member.builder()
                .ssoMemberId(suffix)
                .cohort(new Cohort(1))
                .name(name)
                .status(ActiveStatus.OB)
                .build();
        if (admin) {
            member.promoteToAdmin();
        }
        memberRepository.saveAndFlush(member);
        memberIds.add(member.getId());
        return authenticated(member);
    }

    private AuthenticatedMember createGuest() {
        Member member = memberRepository.saveAndFlush(
                Member.createGuest(Instant.now().plus(Duration.ofHours(1)))
        );
        memberIds.add(member.getId());
        return authenticated(member);
    }

    private AuthenticatedMember authenticated(Member member) {
        return new AuthenticatedMember(
                member.getId(),
                jwtUtil.generateAccessToken(
                        MemberInfoDTO.Companion.from(member),
                        Duration.ofHours(1)
                )
        );
    }

    private HttpResult exchange(HttpMethod method, String path, String token, String body) {
        return restClient.method(method)
                .uri(path)
                .headers(headers -> {
                    if (token != null) {
                        headers.add(HttpHeaders.COOKIE, ACCESS_TOKEN + "=" + token);
                    }
                    if (body != null) {
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    }
                })
                .body(body == null ? "" : body)
                .exchange((request, response) -> new HttpResult(
                        HttpStatus.valueOf(response.getStatusCode().value()),
                        new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)
                ));
    }

    private JsonNode json(HttpResult result) throws IOException {
        return objectMapper.readTree(result.body());
    }

    private void assertError(HttpResult result, HttpStatus status, String code)
            throws IOException {
        assertThat(result.status().value()).isEqualTo(status.value());
        assertThat(json(result).path("errorCode").asText()).isEqualTo(code);
    }

    private String uniqueGroupName(String prefix) {
        return prefix + SEQUENCE.incrementAndGet();
    }

    private String createGroupBody(String name) {
        return """
                {
                  "type": "STUDY",
                  "name": "%s",
                  "introduction": "E2E 그룹",
                  "capacity": 5,
                  "recruitmentMode": "ALWAYS",
                  "recruitmentStartAt": null,
                  "recruitmentEndAt": null
                }
                """.formatted(name);
    }

    private record AuthenticatedMember(long id, String token) {
    }

    private record HttpResult(HttpStatus status, String body) {
    }
}
