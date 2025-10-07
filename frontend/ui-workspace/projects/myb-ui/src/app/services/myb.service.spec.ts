import { TestBed } from '@angular/core/testing';

import { MyBService } from './myb.service';

describe('CommService', () => {
  let service: MyBService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MyBService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
