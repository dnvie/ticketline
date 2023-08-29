package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreatePerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedPerformanceDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.PerformanceMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Performance;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.repository.PerformanceRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.PerformanceService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SimplePerformanceService implements PerformanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final PerformanceRepository performanceRepository;
    private final PerformanceMapper performanceMapper;
    private final SimplePerformanceValidator performanceValidator;
    private final EventRepository eventRepository;


    @Override
    public List<DetailedPerformanceDto> findAll() {
        LOGGER.debug("Find all performances");
        List<Performance> performances = performanceRepository.findAll();
        return this.performanceMapper.performanceToDetailedPerformanceDto(performances);
    }

    @Override
    public DetailedPerformanceDto findById(Long id) throws NotFoundException {
        LOGGER.debug("Find performance with the id {}", id);
        Optional<Performance> performance = performanceRepository.findById(id);
        if (performance.isPresent()) {
            Performance toReturn = performance.get();
            return performanceMapper.performanceToDetailedPerformanceDto(toReturn);
        } else {
            throw new NotFoundException(String.format("Could not find performance with id %s", id));
        }
    }

    @Override
    public DetailedPerformanceDto createPerformance(CreatePerformanceDto performance) throws ValidationException, ConflictException {
        LOGGER.debug("Create new Performance {}", performance);
        //validate
        performanceValidator.validateForCreate(performance);
        //create new performance entity
        Performance toCreate = this.performanceMapper.createPerformanceDtoToPerformance(performance);
        //save new Performance and return
        Performance created = performanceRepository.save(toCreate);
        return findById(created.getId());
    }

    @Override
    public void deleteById(Long id) throws NotFoundException {
        LOGGER.debug("Delete performance with the id {}", id);
        Optional<Performance> performanceOptional = performanceRepository.findById(id);
        if (performanceOptional.isPresent()) {
            Performance performance = performanceOptional.get();
            performanceRepository.delete(performance);
        } else {
            throw new NotFoundException(String.format("Could not find performance with id %s", id));
        }
    }

    @Override
    public DetailedPerformanceDto updatePerformance(Long id, DetailedPerformanceDto detailedPerformanceDto)
        throws NotFoundException, ValidationException, ConflictException {
        LOGGER.debug("updatePerformance({})", id);
        Optional<Performance> performance = performanceRepository.findById(id);
        if (performance.isEmpty()) {
            throw new NotFoundException(String.format("Could not find performance with id %s", id));
        }
        performanceValidator.validateBeforeUpdate(detailedPerformanceDto);

        Performance toUpdate = performance.get();
        Performance newValues = performanceMapper.detailedPerformanceDtoToPerformance(detailedPerformanceDto);
        toUpdate.setTitle(newValues.getTitle());
        toUpdate.setEvent(newValues.getEvent());
        toUpdate.setLocation(newValues.getLocation());
        toUpdate.setPerformers(newValues.getPerformers());
        toUpdate.setStartTime(newValues.getStartTime());
        toUpdate.setEndTime(newValues.getEndTime());
        toUpdate.setPrice(newValues.getPrice());
        toUpdate.setSeatMap(newValues.getSeatMap());
        Performance updated = performanceRepository.save(toUpdate);
        return performanceMapper.performanceToDetailedPerformanceDto(updated);
    }
}
