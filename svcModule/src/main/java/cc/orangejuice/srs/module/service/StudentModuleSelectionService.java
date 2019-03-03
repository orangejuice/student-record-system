package cc.orangejuice.srs.module.service;


import cc.orangejuice.srs.module.client.ProgrammeFeignClient;
import cc.orangejuice.srs.module.client.dto.ProgrammePropDTO;
import cc.orangejuice.srs.module.domain.Module;
import cc.orangejuice.srs.module.domain.ModuleGrade;
import cc.orangejuice.srs.module.domain.StudentModuleSelection;
import cc.orangejuice.srs.module.repository.StudentModuleSelectionRepository;
import cc.orangejuice.srs.module.service.dto.StudentModuleSelectionDTO;
import cc.orangejuice.srs.module.service.mapper.StudentModuleSelectionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;


/**
 * Service Implementation for managing StudentModuleSelection.
 */
@Service
@Transactional
public class StudentModuleSelectionService {

        private final Logger log = LoggerFactory.getLogger(StudentModuleSelectionService.class);

        private final StudentModuleSelectionRepository studentModuleSelectionRepository;

        private final StudentModuleSelectionMapper studentModuleSelectionMapper;

        private final ProgrammeFeignClient programmeFeignClient;

        private final ModuleGradeService moduleGradeService;


        public StudentModuleSelectionService(StudentModuleSelectionRepository studentModuleSelectionRepository,
                                             StudentModuleSelectionMapper studentModuleSelectionMapper,
                                             ProgrammeFeignClient programmeFeignClient,
                                             ModuleGradeService moduleGradeService) {
            this.studentModuleSelectionRepository = studentModuleSelectionRepository;
            this.studentModuleSelectionMapper = studentModuleSelectionMapper;
            this.programmeFeignClient = programmeFeignClient;
            this.moduleGradeService = moduleGradeService;
        }

        /**
         * Save a studentModuleSelection.
         *
         * @param studentModuleSelectionDTO the entity to save
         * @return the persisted entity
         */
        public StudentModuleSelectionDTO save(StudentModuleSelectionDTO studentModuleSelectionDTO) {
            log.debug("Request to save StudentModuleSelection : {}", studentModuleSelectionDTO);
            StudentModuleSelection studentModuleSelection = studentModuleSelectionMapper.toEntity(studentModuleSelectionDTO);
            studentModuleSelection = studentModuleSelectionRepository.save(studentModuleSelection);
            return studentModuleSelectionMapper.toDto(studentModuleSelection);
        }

        /**
         * Get all the studentModuleSelections.
         *
         * @param pageable the pagination information
         * @return the list of entities
         */
        @Transactional(readOnly = true)
        public Page<StudentModuleSelectionDTO> findAll(Pageable pageable) {
            log.debug("Request to get all StudentModuleSelections");
            return studentModuleSelectionRepository.findAll(pageable)
                .map(studentModuleSelectionMapper::toDto);
        }


        /**
         * Get one studentModuleSelection by id.
         *
         * @param id the id of the entity
         * @return the entity
         */
        @Transactional(readOnly = true)
        public Optional<StudentModuleSelectionDTO> findOne(Long id) {
            log.debug("Request to get StudentModuleSelection : {}", id);
            return studentModuleSelectionRepository.findById(id)
                .map(studentModuleSelectionMapper::toDto);
        }

        /**
         * Delete the studentModuleSelection by id.
         *
         * @param id the id of the entity
         */
        public void delete(Long id) {
            log.debug("Request to delete StudentModuleSelection : {}", id);
            studentModuleSelectionRepository.deleteById(id);
        }


        public void updateMarkBySelectionIdAndMark(Long selectionId, Double mark) {
            log.debug("Request to update id: {} StudentModuleSelections with mark {}", selectionId, mark);
            Double creditHour;
            studentModuleSelectionRepository.updateMarksById(selectionId, mark);
            creditHour = getCreditHour(selectionId);
            updateQCS(selectionId, mark, creditHour);
            checkIfSemesterIsEnd(selectionId);
        }

        private void checkIfSemesterIsEnd(Long selectionId) {
            log.debug("Request to get Semester No for selection Id: {}", selectionId);

            Boolean haveAllMarks = true;

            // get a tuple for getting studentId and semesterNo
            Optional<StudentModuleSelectionDTO> studentModuleSelectionDTO = findOne(selectionId);

            log.debug("Request to get result list for student {} at SemesterNo {}", studentModuleSelectionDTO.get().getStudentId(), studentModuleSelectionDTO.get().getSemesterNo());
            List<StudentModuleSelection> resultsList = studentModuleSelectionRepository.findAllByStudentIdAndSemesterNo(studentModuleSelectionDTO.get().getStudentId(), studentModuleSelectionDTO.get().getSemesterNo());

            // check if a student has all the marks for a semester
            for(StudentModuleSelection studentModuleSelection : resultsList) {
                if(studentModuleSelection.getMarks() == null) {
                    haveAllMarks = false;
                    break;
                }
            }
            // (if it is the end of the semester)
            if(haveAllMarks) {
                // send all the results that less or equal to this academic semester
                log.debug("It is the end of semester {}, Request to get all the results that that less or equal to the academic semester {} for student {}", studentModuleSelectionDTO.get().getAcademicSemester(), studentModuleSelectionDTO.get().getAcademicSemester(), studentModuleSelectionDTO.get().getStudentId());
                List<StudentModuleSelection> allResultsList = studentModuleSelectionRepository.findAllByStudentIdAndSemesterNo(studentModuleSelectionDTO.get().getStudentId(), studentModuleSelectionDTO.get().getAcademicSemester());
                List<StudentModuleSelectionDTO> allResulstsDTO = studentModuleSelectionMapper.toDto(allResultsList);
                // todo send to student micro-service for calculating the QCA with resultList, academicYear and academicSemester

            }
        }

        private Double getCreditHour(Long selectionId) {
            log.debug("Request to get credit from a module in the selection {}", selectionId);
            Optional<StudentModuleSelection> studentModuleSelection = studentModuleSelectionRepository.findById(selectionId);
            return studentModuleSelection.get().getModule().getCredit() * getFactor(studentModuleSelection);
        }


        private Double getFactor(Optional<StudentModuleSelection> studentModuleSelection) {
            log.debug("Request to get factor from academicYear: {}, academicSemester: {}, yearNumber: {}, SemesterNumber: {}",
                studentModuleSelection.get().getAcademicYear(),
                studentModuleSelection.get().getAcademicSemester(),
                studentModuleSelection.get().getYearNo(),
                studentModuleSelection.get().getSemesterNo());

            ResponseEntity programmePropResponse = programmeFeignClient.getProgrammeProps("SEMESTER",
                studentModuleSelection.get().getAcademicYear(),
                studentModuleSelection.get().getYearNo(),
                studentModuleSelection.get().getSemesterNo(),
                "factor");

            ProgrammePropDTO programmeProp = (ProgrammePropDTO) programmePropResponse.getBody();

            return Double.parseDouble(programmeProp.getValue());

        }


        private void updateQCS(Long selectionId, Double mark, Double creditHour) {
            log.debug("Request to update QPV and GradeName by Mark: {}, creditHour: {}", mark, creditHour);

            // get QPV and GradeName
            Double qcs;
            String gradeName="";
            ModuleGrade moduleGradeResult = null;

            List<ModuleGrade> moduleGradeList = moduleGradeService.getAllModuleGradewithQcaAffected();
            log.debug("moduleGradeList result: {}", moduleGradeList.size());

            for(ModuleGrade moduleGrade : moduleGradeList) {
                if(mark >= moduleGrade.getLowMarks()) {
                    moduleGradeResult = moduleGrade;
                    break;
                }
            }

            qcs = moduleGradeResult.getQpv() * creditHour;
            DecimalFormat df = new DecimalFormat("#.##");
            qcs = Double.parseDouble(df.format(qcs));
            log.debug("Request to update student result with selectionId: {}, gradeName: {}, QCS: {}, creditHour: {}", selectionId, gradeName, qcs, creditHour);
            studentModuleSelectionRepository.updateById(selectionId, moduleGradeResult, qcs, creditHour);
        }

        public List<StudentModuleSelectionDTO> findAllStudentSelectionsByYear(Long studentId, Integer academicYear, Integer yearNo) {
            log.debug("Request to get Year selections for student: {} at academicYear: {}, yearNo: {}",
                studentId, academicYear, yearNo);
            List<StudentModuleSelection> studentModuleSelections=   studentModuleSelectionRepository.findAllByStudentIdAndAcademicYearAndYearNo(studentId, academicYear, yearNo);
            List<StudentModuleSelectionDTO> returnList =  studentModuleSelectionMapper.toDto(studentModuleSelections);
            return returnList;
        }

}
