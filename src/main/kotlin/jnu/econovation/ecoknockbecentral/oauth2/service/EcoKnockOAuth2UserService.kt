package jnu.econovation.ecoknockbecentral.oauth2.service

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class EcoKnockOAuth2UserService(
//    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        TODO()

//        val oAuth2UserAttributes = super.loadUser(userRequest).attributes
//        val registrationId = userRequest.clientRegistration.registrationId
//        val userNameAttributeName = userRequest.clientRegistration
//            .providerDetails.userInfoEndpoint.userNameAttributeName
//
//        val oauth2MemberInfoDTO = OAuth2MemberInfoDTO.of(
//            registrationId = registrationId,
//            attributes = oAuth2UserAttributes
//        )
//
//        return IsekAIUserDetails(
//            memberInfo = memberService.getOrSave(oauth2MemberInfoDTO),
//            attributes = oAuth2UserAttributes,
//            userNameAttributeName = userNameAttributeName
//        )
    }

}