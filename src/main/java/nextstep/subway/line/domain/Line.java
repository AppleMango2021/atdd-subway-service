package nextstep.subway.line.domain;

import nextstep.subway.BaseEntity;
import nextstep.subway.fare.domain.Fare;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String color;

    @Embedded
    private Sections sections = new Sections();

    @Embedded
    private Fare extraCharge;

    protected Line() {
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Line(String name, String color, Fare extraCharge) {
        this.name = name;
        this.color = color;
        this.extraCharge = extraCharge;
    }

    public Line(String name, String color, Station upStation, Station downStation, int distance) {
        this.name = name;
        this.color = color;
        sections.add(new Section(this, upStation, downStation, distance));
    }

    public Line(String name, String color, Station upStation, Station downStation, int distance, int extraCharge) {
        this.name = name;
        this.color = color;
        sections.add(new Section(this, upStation, downStation, distance));
        this.extraCharge = new Fare(extraCharge);
    }

    public void update(Line line) {
        this.name = line.getName();
        this.color = line.getColor();
        this.extraCharge = line.extraCharge;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Sections getSections() {
        return sections;
    }

    public List<Section> getSectionList() {
        return sections.getSections();
    }

    public List<Station> getSortedStation() {
        return sections.getSortedStation();
    }

    public int getExtraCharge() {
        return extraCharge.getFare();
    }

    public void addSection(Section section) {
        section.addLine(this);
    }

    public void removeStation(Station station) {
        sections.removeStation(station);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Line line = (Line) o;
        return Objects.equals(id, line.id)
                && Objects.equals(name, line.name)
                && Objects.equals(color, line.color)
                && Objects.equals(sections, line.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color, sections);
    }
}
