package nextstep.subway.favorite.service;

import nextstep.subway.exception.CannotAddException;
import nextstep.subway.exception.Message;
import nextstep.subway.favorite.application.FavoriteService;
import nextstep.subway.favorite.domain.Favorite;
import nextstep.subway.favorite.domain.FavoriteRepository;
import nextstep.subway.favorite.dto.FavoriteRequest;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.member.application.MemberService;
import nextstep.subway.member.domain.Member;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static nextstep.subway.TestFixture.*;

@DisplayName("즐겨찾기 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private StationService stationService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;


    private static final Long MEMBER_ID = 1L;
    private static final Long SOURCE_STATION_ID = 1L;
    private static final Long TARGET_STATION_ID = 2L;

    private final Member 사용자 = new Member(EMAIL, PASSWORD, AGE);
    private final Station 강남역 = new Station(SOURCE_STATION_ID, "강남역");
    private final Station 광교역 = new Station(TARGET_STATION_ID, "광교역");
    private final Favorite 강남_광교_즐겨찾기 = new Favorite(MEMBER_ID, 사용자, 강남역, 광교역);

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteService(favoriteRepository, stationService, memberService);
    }

    @DisplayName("즐겨찾기 생성")
    @Test
    void 즐겨찾기_생성_성공() {
        //given
        Map<Long, Station> stations = new HashMap<>();
        stations.put(SOURCE_STATION_ID, 강남역);
        stations.put(TARGET_STATION_ID, 광교역);

        when(memberService.findById(MEMBER_ID)).thenReturn(사용자);
        when(stationService.findStations(SOURCE_STATION_ID, TARGET_STATION_ID)).thenReturn(stations);
        when(favoriteRepository.save(any())).thenReturn(강남_광교_즐겨찾기);

        //when
        FavoriteRequest 강남_광교_요청 = new FavoriteRequest(SOURCE_STATION_ID, TARGET_STATION_ID);
        FavoriteResponse 강남_광교_응답 = favoriteService.createFavorite(MEMBER_ID, 강남_광교_요청);

        //then
        assertThat(강남_광교_응답.getSource().getName()).isEqualTo("강남역");
        assertThat(강남_광교_응답.getTarget().getName()).isEqualTo("광교역");
    }

    @DisplayName("즐겨찾기 목록 조회")
    @Test
    void 즐겨찾기_목록_조회_성공() {
        //given
        Favorite 강남_광교 = new Favorite(사용자, 강남역, 광교역);
        Favorite 회현_명동 = new Favorite(사용자, 회현역, 명동역);

        List<Favorite> 예상_즐겨찾기목록 = new ArrayList<>(Arrays.asList(강남_광교, 회현_명동));
        when(favoriteRepository.findByMemberId(MEMBER_ID)).thenReturn(예상_즐겨찾기목록);

        //when
        List<FavoriteResponse> 실제_즐겨찾기목록 = favoriteService.findFavorites(MEMBER_ID);

        //then
        assertThat(실제_즐겨찾기목록).hasSize(2);
        assertThat(실제_즐겨찾기목록.get(0).getSource()).extracting("name").isEqualTo("강남역");
        assertThat(실제_즐겨찾기목록.get(0).getTarget()).extracting("name").isEqualTo("광교역");
        assertThat(실제_즐겨찾기목록.get(1).getSource()).extracting("name").isEqualTo("회현역");
        assertThat(실제_즐겨찾기목록.get(1).getTarget()).extracting("name").isEqualTo("명동역");
    }

    @DisplayName("즐겨찾기 삭제")
    @Test
    void 즐겨찾기_목록_삭제_성공() {
        //given
        when(favoriteRepository.findByIdAndMemberId(1L, MEMBER_ID)).thenReturn(Optional.of(강남_광교_즐겨찾기));

        //when
        favoriteService.deleteFavorite(MEMBER_ID, 1L);
        List<FavoriteResponse> 즐겨찾기목록 = favoriteService.findFavorites(MEMBER_ID);

        //then
        verify(favoriteRepository).delete(강남_광교_즐겨찾기);
        assertThat(즐겨찾기목록).isEmpty();
    }

    @DisplayName("이미 등록되어 있는 즐겨찾기 다시 추가")
    @Test
    void 예외상황_이미_등록되어_있는_즐겨찾기() {
        //given
        Map<Long, Station> stations = new HashMap<>();
        stations.put(SOURCE_STATION_ID, 강남역);
        stations.put(TARGET_STATION_ID, 광교역);

        when(memberService.findById(MEMBER_ID)).thenReturn(사용자);
        when(stationService.findStations(SOURCE_STATION_ID, TARGET_STATION_ID)).thenReturn(stations);
        when(favoriteRepository.existsBySourceIdAndTargetId(SOURCE_STATION_ID, TARGET_STATION_ID)).thenReturn(true);

        FavoriteRequest 강남_광교_요청 = new FavoriteRequest(SOURCE_STATION_ID, TARGET_STATION_ID);

        //when
        assertThatThrownBy(() -> favoriteService.createFavorite(MEMBER_ID, 강남_광교_요청))
                .isInstanceOf(CannotAddException.class)
                .hasMessage(Message.ERROR_FAVORITE_ALREADY_EXISTS.showText());
    }
}
