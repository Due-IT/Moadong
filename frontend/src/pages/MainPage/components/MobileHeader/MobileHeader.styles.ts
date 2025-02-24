import styled from 'styled-components';

export const MobileHeaderContainer = styled.header`
  display: none;

  @media (max-width: 500px) {
    position: fixed;
    display: flex;
    top: 0;
    left: 0;
    right: 0;
    height: 46px;
    padding: 10px 20px;
    max-width: 500px;
    margin: 0 auto;
    z-index: 2;
    background-color: white;
  }

  @media (max-width: 375px) {
    padding: 0 10px;
  }
`;

export const MobileHeaderWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
`;

export const MobileMainIcon = styled.button`
  width: 90px;
  height: 26px;
  background-color: transparent;
  border: none;
  cursor: pointer;

  img {
    width: 90px;
    height: auto;
    object-fit: cover;
  }
`;

export const MobileSubContainer = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 71px;
  height: 24px;
`;

export const MobileSearchIcon = styled.button`
  width: 23px;
  height: 23px;
  background-color: transparent;
  border: none;
  cursor: pointer;

  img {
    width: 23px;
    height: auto;
    object-fit: cover;
  }
`;

export const MobileMenu = styled.button`
  width: 23px;
  height: 23px;
  background-color: transparent;
  border: none;
  cursor: pointer;

  img {
    width: 23px;
    height: auto;
    object-fit: cover;
  }
`;
