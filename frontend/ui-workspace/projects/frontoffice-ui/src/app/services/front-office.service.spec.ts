import { TestBed } from '@angular/core/testing';

import { FrontofficeService } from './front-office.service';

describe('CommService', () => {
  let service: FrontOfficeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FrontOfficeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
