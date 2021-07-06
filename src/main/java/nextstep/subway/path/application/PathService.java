package nextstep.subway.path.application;

import nextstep.subway.auth.domain.User;
import nextstep.subway.fare.FareCalculator;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Lines;
import nextstep.subway.path.domain.Path;
import nextstep.subway.path.domain.PathFinder;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class PathService {

    private final StationService stationService;
    private final LineRepository lineRepository;

    public PathService(StationService stationService, LineRepository lineRepository) {
        this.stationService = stationService;
        this.lineRepository = lineRepository;
    }

    @Transactional(readOnly = true)
    public PathResponse findShortestPath(User user, Long start, Long end) {
        Map<Long, Station> stations = stationService.findStations(start, end);

        Station startStation = stations.get(start);
        Station endStation = stations.get(end);
        Lines lines = new Lines(lineRepository.findAll());

        Path shortestPath = new PathFinder(lines).getDijkstraShortestPath(startStation, endStation);
        FareCalculator fareCalculator = new FareCalculator(user, shortestPath.getDistance(), shortestPath.getMaxExtraCharge());

        return PathResponse.of(shortestPath, fareCalculator.calculate());
    }
}
