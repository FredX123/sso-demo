import { TestBed } from '@angular/core/testing';

import { SadaService } from './sada.service';

describe('SadaService', () => {
  let service: SadaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SadaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
