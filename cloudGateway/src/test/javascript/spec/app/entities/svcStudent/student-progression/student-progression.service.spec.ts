/* tslint:disable max-line-length */
import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { take, map } from 'rxjs/operators';
import { StudentProgressionService } from 'app/entities/svcStudent/student-progression/student-progression.service';
import {
    IStudentProgression,
    StudentProgression,
    ProgressType,
    ProgressDecision
} from 'app/shared/model/svcStudent/student-progression.model';

describe('Service Tests', () => {
    describe('StudentProgression Service', () => {
        let injector: TestBed;
        let service: StudentProgressionService;
        let httpMock: HttpTestingController;
        let elemDefault: IStudentProgression;
        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [HttpClientTestingModule]
            });
            injector = getTestBed();
            service = injector.get(StudentProgressionService);
            httpMock = injector.get(HttpTestingController);

            elemDefault = new StudentProgression(0, 0, 0, 0, 0, ProgressType.SEMESTER, ProgressDecision.PASS);
        });

        describe('Service methods', async () => {
            it('should find an element', async () => {
                const returnedFromService = Object.assign({}, elemDefault);
                service
                    .find(123)
                    .pipe(take(1))
                    .subscribe(resp => expect(resp).toMatchObject({ body: elemDefault }));

                const req = httpMock.expectOne({ method: 'GET' });
                req.flush(JSON.stringify(returnedFromService));
            });

            it('should create a StudentProgression', async () => {
                const returnedFromService = Object.assign(
                    {
                        id: 0
                    },
                    elemDefault
                );
                const expected = Object.assign({}, returnedFromService);
                service
                    .create(new StudentProgression(null))
                    .pipe(take(1))
                    .subscribe(resp => expect(resp).toMatchObject({ body: expected }));
                const req = httpMock.expectOne({ method: 'POST' });
                req.flush(JSON.stringify(returnedFromService));
            });

            it('should update a StudentProgression', async () => {
                const returnedFromService = Object.assign(
                    {
                        forAcademicYear: 1,
                        forAcademicSemester: 1,
                        forPartNo: 1,
                        qca: 1,
                        progressType: 'BBBBBB',
                        progressDecision: 'BBBBBB'
                    },
                    elemDefault
                );

                const expected = Object.assign({}, returnedFromService);
                service
                    .update(expected)
                    .pipe(take(1))
                    .subscribe(resp => expect(resp).toMatchObject({ body: expected }));
                const req = httpMock.expectOne({ method: 'PUT' });
                req.flush(JSON.stringify(returnedFromService));
            });

            it('should return a list of StudentProgression', async () => {
                const returnedFromService = Object.assign(
                    {
                        forAcademicYear: 1,
                        forAcademicSemester: 1,
                        forPartNo: 1,
                        qca: 1,
                        progressType: 'BBBBBB',
                        progressDecision: 'BBBBBB'
                    },
                    elemDefault
                );
                const expected = Object.assign({}, returnedFromService);
                service
                    .query(expected)
                    .pipe(
                        take(1),
                        map(resp => resp.body)
                    )
                    .subscribe(body => expect(body).toContainEqual(expected));
                const req = httpMock.expectOne({ method: 'GET' });
                req.flush(JSON.stringify([returnedFromService]));
                httpMock.verify();
            });

            it('should delete a StudentProgression', async () => {
                const rxPromise = service.delete(123).subscribe(resp => expect(resp.ok));

                const req = httpMock.expectOne({ method: 'DELETE' });
                req.flush({ status: 200 });
            });
        });

        afterEach(() => {
            httpMock.verify();
        });
    });
});
